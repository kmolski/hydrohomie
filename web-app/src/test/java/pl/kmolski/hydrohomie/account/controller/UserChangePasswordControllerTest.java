package pl.kmolski.hydrohomie.account.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
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
import reactor.core.publisher.Mono;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;


@WithMockUser
@Import(SecurityConfiguration.class)
@WebFluxTest(UserChangePasswordController.class)
class UserChangePasswordControllerTest {

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
    @WithMockUser(roles = {"ADMIN"})
    void endpointsAreNotAccessibleToAdmin() {
        webTestClient.get().uri("/user/changePassword").exchange()
                .expectStatus().isForbidden();

        webTestClient.post().uri("/user/changePassword").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithAnonymousUser
    void endpointsAreNotAccessibleToUnauth() {
        webTestClient.get().uri("/user/changePassword").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");

        webTestClient.post().uri("/user/changePassword").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void changePasswordFormIsAccessibleByUser() {
        webTestClient.get().uri("/user/changePassword").exchange().expectStatus().isOk();
    }

    @Test
    void changePasswordActionWithCorrectDataSucceeds() {
        var username = "user";
        var newPassword = new PlaintextPassword("enterprise");
        var newEncodedPassword = passwordEncoder.encode(newPassword.plaintext());

        var account = new UserAccount(username, newEncodedPassword, true);
        when(userService.updatePassword(username, newPassword)).thenReturn(Mono.just(account));

        webTestClient.mutateWith(csrf()).post().uri("/user/changePassword")
                .body(fromFormData("password", newPassword.plaintext()).with("pwConfirm", newPassword.plaintext()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));
    }

    @Test
    void changePasswordActionWithoutCsrfTokenFails() {
        var password = "password";

        webTestClient.post().uri("/user/changePassword")
                .body(fromFormData("password", password).with("pwConfirm", password))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void changePasswordActionWithIncorrectPasswordsFails() {
        var password = "password";
        var confirm = "differentFromPassword";

        webTestClient.mutateWith(csrf()).post().uri("/user/changePassword")
                .body(fromFormData("password", password).with("pwConfirm", confirm))
                .exchange()
                .expectBody(String.class)
                .value(allOf(containsString("The password must be at least 10 characters long"),
                             containsString("The passwords do not match")));
    }
}
