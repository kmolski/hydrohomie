package pl.kmolski.hydrohomie.coaster.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import pl.kmolski.hydrohomie.account.service.UserService;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.service.CoasterManagementService;
import pl.kmolski.hydrohomie.testutil.WebFluxControllerTest;
import pl.kmolski.hydrohomie.webmvc.exception.EntityNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser(roles = {"ADMIN"})
@WebFluxTest(AdminCoasterManagementController.class)
class AdminCoasterManagementControllerTest extends WebFluxControllerTest {

    @MockBean
    private CoasterManagementService coasterService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    void endpointsAreNotAccessibleToUser() {
        webTestClient.get().uri("/admin/coasters").exchange()
                .expectStatus().isForbidden();
        webTestClient.get().uri("/admin/coasters/assignCoaster/foo").exchange()
                .expectStatus().isForbidden();

        webTestClient.post().uri("/admin/coasters/assignCoaster/foo").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithAnonymousUser
    void endpointsAreNotAccessibleToUnauth() {
        webTestClient.get().uri("/admin/coasters").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
        webTestClient.get().uri("/admin/coasters/assignCoaster/foo").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");

        webTestClient.post().uri("/admin/coasters/assignCoaster/foo").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void homepageIsAccessibleByAdmin() {
        var coasterNames = Set.of("esp001", "esp002", "esp003");
        var coasters = coasterNames.stream().map(Coaster::new).toList();
        var page = new PageImpl<>(coasters);

        when(coasterService.getUnassignedCoasters(any())).thenReturn(Mono.just(page));
        webTestClient.get().uri("/admin/coasters").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(stringContainsInOrder(coasterNames));
    }

    @Test
    void assignCoasterFormIsAccessibleByAdmin() {
        var usernames = Set.of("spock", "jimmy", "scotty");
        var users = usernames.stream().map(name -> new UserAccount(name, "", true)).toList();
        var page = new PageImpl<>(users);

        when(userService.getAllAccounts(any())).thenReturn(Mono.just(page));
        webTestClient.get().uri("/admin/coasters/assignCoaster/foo").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(stringContainsInOrder(usernames));
    }

    @Test
    void assignCoasterActionWithCorrectDataSucceeds() {
        var username = "james_t_kirk";
        var coasterName = "esp001";

        var coaster = new Coaster(coasterName);
        when(coasterService.assignCoasterToUser(coasterName, username)).thenReturn(Mono.just(coaster));

        webTestClient.mutateWith(csrf()).post()
                .uri("/admin/coasters/assignCoaster/" + coasterName + "?userId=" + username)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));
    }

    @Test
    void assignCoasterActionWithoutCsrfTokenFails() {
        var username = "james_t_kirk";
        var coasterName = "esp001";

        var coaster = new Coaster(coasterName);
        when(coasterService.assignCoasterToUser(coasterName, username)).thenReturn(Mono.just(coaster));

        webTestClient.post()
                .uri("/admin/coasters/assignCoaster/" + coasterName + "?userId=" + username)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void assignCoasterActionWithNonexistentCoasterFails() {
        var username = "james_t_kirk";
        var coasterName = "esp001";

        when(coasterService.assignCoasterToUser(coasterName, username)).thenThrow(EntityNotFoundException.class);
        webTestClient.mutateWith(csrf()).post()
                .uri("/admin/coasters/assignCoaster/" + coasterName + "?userId=" + username)
                .exchange()
                .expectStatus().isNotFound();
    }
}
