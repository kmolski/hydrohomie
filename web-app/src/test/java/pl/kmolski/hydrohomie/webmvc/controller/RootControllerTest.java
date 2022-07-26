package pl.kmolski.hydrohomie.webmvc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.test.context.support.WithMockUser;
import pl.kmolski.hydrohomie.testutil.WebFluxControllerTest;

@WebFluxTest(RootController.class)
class RootControllerTest extends WebFluxControllerTest {

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void homepageRedirectsAdminToUsers() {
        webTestClient.get().uri("/").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/admin");
    }

    @Test
    @WithMockUser
    void homepageRedirectsUserToCoasters() {
        webTestClient.get().uri("/").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/user");
    }

    @Test
    void homepageRedirectsUnauthToLogin() {
        webTestClient.get().uri("/").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }

    @Test
    void login() {
        webTestClient.get().uri("/login").exchange().expectStatus().isOk();
    }
}
