package pl.kmolski.hydrohomie.coaster.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.service.CoasterManagementService;
import pl.kmolski.hydrohomie.coaster.service.CoasterService;
import pl.kmolski.hydrohomie.testutil.WebFluxControllerTest;
import pl.kmolski.hydrohomie.webmvc.exception.EntityNotFoundException;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@WithMockUser
@WebFluxTest(UserCoasterController.class)
class UserCoasterControllerTest extends WebFluxControllerTest {

    @MockBean
    private CoasterService coasterService;

    @MockBean
    private CoasterManagementService coasterManagementService;

    @MockBean
    private Clock clock;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void endpointsAreNotAccessibleToAdmin() {
        webTestClient.get().uri("/user").exchange()
                .expectStatus().isForbidden();
        webTestClient.get().uri("/user/coaster/foo").exchange()
                .expectStatus().isForbidden();
        webTestClient.get().uri("/user/editCoaster/foo").exchange()
                .expectStatus().isForbidden();
        webTestClient.get().uri("/user/removeCoaster/foo").exchange()
                .expectStatus().isForbidden();
        webTestClient.get().uri("/user/coaster/foo/measurements").exchange()
                .expectStatus().isForbidden();
        webTestClient.get().uri("/user/coaster/foo/latestMeasurements").exchange()
                .expectStatus().isForbidden();

        webTestClient.post().uri("/user/editCoaster/foo").exchange()
                .expectStatus().isForbidden();
        webTestClient.post().uri("/user/removeCoaster/foo").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithAnonymousUser
    void endpointsAreNotAccessibleToUnauth() {
        webTestClient.get().uri("/user").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
        webTestClient.get().uri("/user/coaster/foo").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
        webTestClient.get().uri("/user/editCoaster/foo").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
        webTestClient.get().uri("/user/removeCoaster/foo").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
        webTestClient.get().uri("/user/coaster/foo/measurements").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
        webTestClient.get().uri("/user/coaster/foo/latestMeasurements").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");

        webTestClient.post().uri("/user/editCoaster/foo").exchange()
                .expectStatus().isForbidden();
        webTestClient.post().uri("/user/removeCoaster/foo").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void homepageIsAccessibleByUser() {
        var coasterNames = Set.of("esp001", "esp002", "esp003");
        var coasters = coasterNames.stream()
                .map(name -> new Coaster(name).setTimezone(ZoneId.of("Etc/UTC")).setInactiveSince(Instant.EPOCH))
                .toList();
        var page = new PageImpl<>(coasters);

        when(coasterManagementService.getUserAssignedCoasters(any(), any())).thenReturn(Mono.just(page));
        webTestClient.get().uri("/user").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(stringContainsInOrder(coasterNames));
    }

    @Test
    void showCoasterDetails() {
        var deviceName = "esp001";
        var displayName = "My personal coaster";
        var coaster = new Coaster(deviceName).setDisplayName(displayName);

        when(coasterManagementService.getCoasterDetails(eq(deviceName), any())).thenReturn(Mono.just(coaster));
        webTestClient.get().uri("/user/coaster/" + deviceName).exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString(displayName));
    }

    @Test
    void editCoasterFormIsAccessibleByUser() {
        var deviceName = "esp001";
        var coaster = new Coaster(deviceName);

        when(coasterManagementService.getCoasterDetails(eq(deviceName), any())).thenReturn(Mono.just(coaster));
        webTestClient.get().uri("/user/editCoaster/" + deviceName).exchange().expectStatus().isOk();
    }

    @Test
    void editCoasterActionWithCorrectDataSucceeds() {
        var deviceName = "esp001";

        var newDisplayName = "My personal coaster";
        var newDescription = "Some description";
        var newTimezone = ZoneId.of("Europe/Warsaw");
        var newPlace = "my room";

        var newCoaster = new Coaster(deviceName)
                .setDisplayName(newDisplayName)
                .setDescription(newDescription)
                .setTimezone(newTimezone)
                .setPlace(newPlace);
        when(coasterManagementService.updateCoasterDetails(eq(deviceName), any(), any())).thenReturn(Mono.just(newCoaster));

        webTestClient.mutateWith(csrf()).post().uri("/user/editCoaster/" + deviceName)
                .body(fromFormData("displayName", newDisplayName)
                        .with("description", newDescription)
                        .with("timezone", newTimezone.toString())
                        .with("place", newPlace))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));
    }

    @Test
    void editCoasterActionWithoutCsrfTokenFails() {
        var newDisplayName = "My personal coaster";
        var newDescription = "Some description";
        var newTimezone = ZoneId.of("Europe/Warsaw");
        var newPlace = "my room";

        webTestClient.post().uri("/user/editCoaster/foo")
                .body(fromFormData("displayName", newDisplayName)
                        .with("description", newDescription)
                        .with("timezone", newTimezone.toString())
                        .with("place", newPlace))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void editCoasterActionWithIncorrectFieldsFails() {
        var deviceName = "esp001";
        var newDisplayName = "My personal coaster".repeat(500);
        var newDescription = "Some description".repeat(500);
        var newPlace = "my room".repeat(500);

        webTestClient.mutateWith(csrf()).post().uri("/user/editCoaster/" + deviceName)
                .body(fromFormData("displayName", newDisplayName)
                        .with("description", newDescription)
                        .with("place", newPlace))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(allOf(containsString("The display name must be at most 127 characters long"),
                             containsString("The description must be at most 511 characters long"),
                             containsString("The place must be at most 127 characters long")));
    }

    @Test
    void removeCoasterConfirmationIsAccessibleByUser() {
        webTestClient.get().uri("/user/removeCoaster/foo").exchange().expectStatus().isOk();
    }

    @Test
    void removeCoasterActionWithExistingCoasterSucceeds() {
        var coasterName = "esp001";

        var coaster = new Coaster(coasterName);
        when(coasterManagementService.removeCoasterFromUser(eq(coasterName), any())).thenReturn(Mono.just(coaster));

        webTestClient.mutateWith(csrf()).post().uri("/user/removeCoaster/" + coasterName)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));
    }

    @Test
    void removeCoasterActionWithoutCsrfTokenFails() {
        var coasterName = "esp001";

        webTestClient.post().uri("/user/removeCoaster/" + coasterName)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void removeCoasterActionWithNonexistentCoasterFails() {
        var coasterName = "esp001";

        when(coasterManagementService.removeCoasterFromUser(eq(coasterName), any())).thenThrow(EntityNotFoundException.class);
        webTestClient.mutateWith(csrf()).post().uri("/user/removeCoaster/" + coasterName)
                .exchange()
                .expectStatus().isNotFound();
    }
}
