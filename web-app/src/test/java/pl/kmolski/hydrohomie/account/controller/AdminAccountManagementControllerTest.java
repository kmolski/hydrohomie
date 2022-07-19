package pl.kmolski.hydrohomie.account.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.kmolski.hydrohomie.account.dto.PlaintextPassword;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import pl.kmolski.hydrohomie.account.service.UserService;
import pl.kmolski.hydrohomie.webmvc.config.SecurityConfiguration;
import pl.kmolski.hydrohomie.webmvc.exception.EntityNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;


@WithMockUser(roles = {"ADMIN"})
@Import(SecurityConfiguration.class)
@WebFluxTest(AdminAccountManagementController.class)
class AdminAccountManagementControllerTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserService userService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setupWebTestClient() {
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
                .build();
    }

    @Test
    @WithMockUser
    void endpointsAreNotAccessibleToUser() {
        webTestClient.get().uri("/admin").exchange()
                .expectStatus().isForbidden();
        webTestClient.get().uri("/admin/createUser").exchange()
                .expectStatus().isForbidden();
        webTestClient.get().uri("/admin/changePassword/foo").exchange()
                .expectStatus().isForbidden();

        webTestClient.post().uri("/admin/createUser").exchange()
                .expectStatus().isForbidden();
        webTestClient.post().uri("/admin/changePassword/foo").exchange()
                .expectStatus().isForbidden();
        webTestClient.post().uri("/admin/setEnabledUser/foo").exchange()
                .expectStatus().isForbidden();
        webTestClient.post().uri("/admin/deleteUser/foo").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithAnonymousUser
    void endpointsAreNotAccessibleToUnauth() {
        webTestClient.get().uri("/admin").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
        webTestClient.get().uri("/admin/createUser").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
        webTestClient.get().uri("/admin/changePassword/foo").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");

        webTestClient.post().uri("/admin/createUser").exchange()
                .expectStatus().isForbidden();
        webTestClient.post().uri("/admin/changePassword/foo").exchange()
                .expectStatus().isForbidden();
        webTestClient.post().uri("/admin/setEnabledUser/foo").exchange()
                .expectStatus().isForbidden();
        webTestClient.post().uri("/admin/deleteUser/foo").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createUserFormIsAccessibleByAdmin() {
        webTestClient.get().uri("/admin/createUser").exchange().expectStatus().isOk();
    }

    @Test
    void createUserActionWithCorrectDataSucceeds() {
        var username = "james_t_kirk";
        var password = new PlaintextPassword("enterprise");
        var encodedPassword = passwordEncoder.encode(password.plaintext());

        var account = new UserAccount(username, encodedPassword, true);
        when(userService.createAccount(username, password, true)).thenReturn(Mono.just(account));

        webTestClient.mutateWith(csrf()).post().uri("/admin/createUser")
                .body(fromFormData("username", username)
                        .with("password", password.plaintext())
                        .with("pwConfirm", password.plaintext())
                        .with("enabled", "true"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));
    }

    @Test
    void createUserActionWithoutCsrfTokenFails() {
        var username = "james_t_kirk";
        var password = "password";

        webTestClient.post().uri("/admin/createUser")
                .body(fromFormData("username", username)
                        .with("password", password)
                        .with("pwConfirm", password)
                        .with("enabled", "true"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createUserActionWithIncorrectUsernameFails() {
        var username = "james???kirk".repeat(500);
        var password = new PlaintextPassword("enterprise");

        webTestClient.mutateWith(csrf()).post().uri("/admin/createUser")
                .body(fromFormData("username", username)
                        .with("password", password.plaintext())
                        .with("pwConfirm", password.plaintext())
                        .with("enabled", "true"))
                .exchange()
                .expectBody(String.class)
                .value(allOf(containsString("The username can only contain letters, numbers and _ separators"),
                             containsString("The username must be at most 63 characters long")));
    }

    @Test
    void createUserActionWithIncorrectPasswordsFails() {
        var username = "james_t_kirk";
        var password = "password";
        var confirm = "differentFromPassword";

        webTestClient.mutateWith(csrf()).post().uri("/admin/createUser")
                .body(fromFormData("username", username)
                        .with("password", password)
                        .with("pwConfirm", confirm)
                        .with("enabled", "true"))
                .exchange()
                .expectBody(String.class)
                .value(allOf(containsString("The password must be at least 10 characters long"),
                             containsString("The passwords do not match")));
    }

    @Test
    void homepageIsAccessibleByAdmin() {
        var usernames = Set.of("spock", "jimmy", "scotty");
        var users = usernames.stream().map(name -> new UserAccount(name, "", true)).toList();
        var page = new PageImpl<>(users);

        when(userService.getAllAccounts(any())).thenReturn(Mono.just(page));
        webTestClient.get().uri("/admin").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(stringContainsInOrder(usernames));
    }

    @Test
    void changePasswordFormIsAccessibleByAdmin() {
        webTestClient.get().uri("/admin/changePassword/foo").exchange().expectStatus().isOk();
    }

    @Test
    void changePasswordActionWithCorrectDataSucceeds() {
        var username = "james_t_kirk";
        var newPassword = new PlaintextPassword("enterprise");
        var newEncodedPassword = passwordEncoder.encode(newPassword.plaintext());

        var account = new UserAccount(username, newEncodedPassword, true);
        when(userService.updatePassword(username, newPassword)).thenReturn(Mono.just(account));

        webTestClient.mutateWith(csrf()).post().uri("/admin/changePassword/" + username)
                .body(fromFormData("password", newPassword.plaintext()).with("pwConfirm", newPassword.plaintext()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));
    }

    @Test
    void changePasswordActionWithoutCsrfTokenFails() {
        var username = "james_t_kirk";
        var password = "password";

        webTestClient.post().uri("/admin/changePassword/" + username)
                .body(fromFormData("password", password).with("pwConfirm", password))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void changePasswordActionWithIncorrectPasswordsFails() {
        var username = "james_t_kirk";
        var password = "password";
        var confirm = "differentFromPassword";

        webTestClient.mutateWith(csrf()).post().uri("/admin/changePassword/" + username)
                .body(fromFormData("password", password).with("pwConfirm", confirm))
                .exchange()
                .expectBody(String.class)
                .value(allOf(containsString("The password must be at least 10 characters long"),
                             containsString("The passwords do not match")));
    }

    @Test
    void changePasswordActionWithNonexistentUserFails() {
        var username = "james_t_kirk";
        var newPassword = new PlaintextPassword("enterprise");

        when(userService.updatePassword(username, newPassword)).thenThrow(EntityNotFoundException.class);

        webTestClient.mutateWith(csrf()).post().uri("/admin/changePassword/" + username)
                .body(fromFormData("password", newPassword.plaintext()).with("pwConfirm", newPassword.plaintext()))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void setEnabledUserActionWithExistingUserSucceeds() {
        var username = "james_t_kirk";

        var account = new UserAccount(username, null, false);
        when(userService.setAccountEnabled(username, false)).thenReturn(Mono.just(account));

        webTestClient.mutateWith(csrf()).post().uri("/admin/setEnabledUser/" + username + "?enabled=false")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));
    }

    @Test
    void setEnabledUserActionWithoutCsrfTokenFails() {
        var username = "james_t_kirk";

        webTestClient.post().uri("/admin/setEnabledUser/" + username + "?enabled=false")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void setEnabledUserActionWithNonexistentUserFails() {
        var username = "james_t_kirk";

        when(userService.setAccountEnabled(username, false)).thenThrow(EntityNotFoundException.class);

        webTestClient.mutateWith(csrf()).post().uri("/admin/setEnabledUser/" + username + "?enabled=false")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteUserActionWithExistingUserSucceeds() {
        var username = "james_t_kirk";

        var account = new UserAccount(username, null, false);
        when(userService.deleteAccount(username)).thenReturn(Mono.just(account));

        webTestClient.mutateWith(csrf()).post().uri("/admin/deleteUser/" + username)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));
    }

    @Test
    void deleteUserActionWithoutCsrfTokenFails() {
        var username = "james_t_kirk";

        webTestClient.post().uri("/admin/deleteUser/" + username)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void deleteUserActionWithNonexistentUserFails() {
        var username = "james_t_kirk";

        when(userService.deleteAccount(username)).thenThrow(EntityNotFoundException.class);

        webTestClient.mutateWith(csrf()).post().uri("/admin/deleteUser/" + username)
                .exchange()
                .expectStatus().isNotFound();
    }
}
