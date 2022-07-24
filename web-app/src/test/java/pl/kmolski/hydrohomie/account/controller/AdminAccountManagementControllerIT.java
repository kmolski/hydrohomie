package pl.kmolski.hydrohomie.account.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.kmolski.hydrohomie.account.dto.PlaintextPassword;
import pl.kmolski.hydrohomie.account.repo.UserRepository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
class AdminAccountManagementControllerIT {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14.2");

    @Autowired
    private ApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private WebTestClient webTestClient;

    static String getR2dbcUrl() {
        return String.format(
                "r2dbc:postgresql://%s:%s/%s",
                POSTGRES.getHost(),
                POSTGRES.getMappedPort(5432),
                POSTGRES.getDatabaseName()
        );
    }

    @DynamicPropertySource
    static void setupPostgresContainer(DynamicPropertyRegistry propertyRegistry) {
        POSTGRES.start();

        propertyRegistry.add("spring.liquibase.url", POSTGRES::getJdbcUrl);
        propertyRegistry.add("spring.liquibase.user", POSTGRES::getUsername);
        propertyRegistry.add("spring.liquibase.password", POSTGRES::getPassword);

        propertyRegistry.add("spring.r2dbc.url", AdminAccountManagementControllerIT::getR2dbcUrl);
        propertyRegistry.add("spring.r2dbc.username", POSTGRES::getUsername);
        propertyRegistry.add("spring.r2dbc.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void setupWebTestClient() {
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
                .build();
    }

    @Test
    void createUserActionInsertsAccountRecord() {
        var username = "james_t_kirk000";
        var password = new PlaintextPassword("enterprise");

        webTestClient.mutateWith(csrf()).post().uri("/admin/createUser")
                .body(fromFormData("username", username)
                        .with("password", password.plaintext())
                        .with("pwConfirm", password.plaintext())
                        .with("enabled", "true"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));

        var account = userRepository.findById(username).block();
        assertNotNull(account, "New account does not exist");
        assertEquals(username, account.getUsername(), "Usernames do not match");
        assertTrue(passwordEncoder.matches(password.plaintext(), account.getPassword()), "Passwords do not match");
        assertTrue(account.isEnabled(), "New account is not enabled");
    }

    @Test
    void changePasswordActionUpdatesPasswordColumn() {
        var username = "james_t_kirk001";
        var oldEncodedPassword = passwordEncoder.encode("enterprise");
        userRepository.create(username, oldEncodedPassword, true).block();

        var newPassword = new PlaintextPassword("troubling tribbles");
        webTestClient.mutateWith(csrf()).post().uri("/admin/changePassword/" + username)
                .body(fromFormData("password", newPassword.plaintext()).with("pwConfirm", newPassword.plaintext()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));

        var account = userRepository.findById(username).block();
        assertNotNull(account, "User account does not exist");
        assertNotEquals(oldEncodedPassword, account.getPassword(), "Password did not change");
        assertTrue(passwordEncoder.matches(newPassword.plaintext(), account.getPassword()), "New password is not set");
    }

    @Test
    void setEnabledUserActionUpdatesEnabledColumn() {
        var username = "james_t_kirk002";
        var encodedPassword = passwordEncoder.encode("enterprise");
        userRepository.create(username, encodedPassword, true).block();

        webTestClient.mutateWith(csrf()).post().uri("/admin/setEnabledUser/" + username + "?enabled=false")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));

        var account = userRepository.findById(username).block();
        assertNotNull(account, "User account does not exist");
        assertFalse(account.isEnabled(), "Account was not disabled");
    }

    @Test
    void deleteUserActionRemovesAccountRecord() {
        var username = "james_t_kirk003";
        var encodedPassword = passwordEncoder.encode("enterprise");
        userRepository.create(username, encodedPassword, true).block();

        webTestClient.mutateWith(csrf()).post().uri("/admin/deleteUser/" + username)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));

        var account = userRepository.findById(username).block();
        assertNull(account, "User account was not removed");
    }

    @Test
    void createUserActionWithDuplicateUsernameFails() {
        var username = "james_t_kirk004";
        var password = new PlaintextPassword("enterprise");
        var encodedPassword = passwordEncoder.encode(password.plaintext());
        userRepository.create(username, encodedPassword, true).block();

        webTestClient.mutateWith(csrf()).post().uri("/admin/createUser")
                .body(fromFormData("username", username)
                        .with("password", password.plaintext())
                        .with("pwConfirm", password.plaintext())
                        .with("enabled", "true"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("User with this username already exists"));
    }
}
