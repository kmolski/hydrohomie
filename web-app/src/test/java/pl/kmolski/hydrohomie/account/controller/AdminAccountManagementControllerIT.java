package pl.kmolski.hydrohomie.account.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import pl.kmolski.hydrohomie.account.dto.PlaintextPassword;
import pl.kmolski.hydrohomie.account.repo.UserRepository;
import pl.kmolski.hydrohomie.testutil.WebFluxControllerIT;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@WithMockUser(roles = {"ADMIN"})
class AdminAccountManagementControllerIT extends WebFluxControllerIT {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

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
