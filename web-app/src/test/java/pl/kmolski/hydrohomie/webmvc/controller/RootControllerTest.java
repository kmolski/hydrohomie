package pl.kmolski.hydrohomie.webmvc.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.kmolski.hydrohomie.webmvc.config.SecurityConfiguration;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@WebFluxTest(RootController.class)
@Import(SecurityConfiguration.class)
class RootControllerTest {

    @Autowired
    private ApplicationContext context;

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
