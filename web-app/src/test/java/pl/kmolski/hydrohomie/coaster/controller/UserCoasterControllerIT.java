package pl.kmolski.hydrohomie.coaster.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import pl.kmolski.hydrohomie.account.repo.UserRepository;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.coaster.repo.MeasurementRepository;
import pl.kmolski.hydrohomie.coaster.service.CoasterManagementService;
import pl.kmolski.hydrohomie.testutil.WebFluxControllerIT;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@WithMockUser(username = UserCoasterControllerIT.USERNAME, roles = {"ADMIN", "USER"})
class UserCoasterControllerIT extends WebFluxControllerIT {

    @Autowired
    private CoasterManagementService coasterManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoasterRepository coasterRepository;

    @Autowired
    private MeasurementRepository measurementRepository;

    static final String USERNAME = "james_t_kirk300";

    @BeforeEach
    void setupTestAccount() {
        if (userRepository.findById(USERNAME).blockOptional().isEmpty()) {
            userRepository.create(USERNAME, "enterprise", true).block();
        }
    }

    @Test
    void getMeasurementsReturnsMeasurementsGroupedByTimeUnit() {
        var coasterName = "esp100";
        coasterRepository.create(coasterName, Instant.EPOCH, ZoneId.of("Etc/UTC")).block();
        coasterManagementService.assignCoasterToUser(coasterName, USERNAME).block();

        measurementRepository.saveAll(Flux.fromStream(IntStream.range(0, 200).boxed())
                .map(i -> new Measurement(null, coasterName, i, Instant.EPOCH.plus(i, ChronoUnit.HOURS)))).blockLast();

        var result = webTestClient.mutateWith(csrf()).get()
                .uri(uriBuilder -> uriBuilder.path("/user/coaster/" + coasterName + "/measurements")
                        .queryParam("start", 0)
                        .queryParam("end", Duration.of(5, ChronoUnit.DAYS).getSeconds() * 1000)
                        .queryParam("unit", "DAYS")
                        .queryParam("tz", "Etc/UTC")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Measurement.class);

        var expectedMeasurements = List.of(
                new Measurement(null, null, 276.0f, Instant.EPOCH),
                new Measurement(null, null, 852.0f, Instant.EPOCH.plus(1, ChronoUnit.DAYS)),
                new Measurement(null, null, 1428.0f, Instant.EPOCH.plus(2, ChronoUnit.DAYS)),
                new Measurement(null, null, 2004.0f, Instant.EPOCH.plus(3, ChronoUnit.DAYS)),
                new Measurement(null, null, 2580.0f, Instant.EPOCH.plus(4, ChronoUnit.DAYS)),
                new Measurement(null, null, 120.0f, Instant.EPOCH.plus(5, ChronoUnit.DAYS))
        );
        assertEquals(expectedMeasurements, result.returnResult().getResponseBody(), "Unexpected measurement group");
    }

    @Test
    void getLatestMeasurementsReturnsLast10Measurements() {
        var coasterName = "esp101";
        coasterRepository.create(coasterName, Instant.EPOCH, ZoneId.of("Etc/UTC")).block();
        coasterManagementService.assignCoasterToUser(coasterName, USERNAME).block();

        var measurements = measurementRepository.saveAll(Flux.fromStream(IntStream.range(0, 20).boxed())
                .map(i -> new Measurement(null, coasterName, i, Instant.ofEpochSecond(i)))).collectList().block();

        var result = webTestClient.mutateWith(csrf()).get()
                .uri("/user/coaster/" + coasterName + "/latestMeasurements")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Measurement.class);

        var expectedMeasurements = measurements.stream().skip(10)
                .sorted(Comparator.comparing(Measurement::timestamp).reversed()).toList();
        assertEquals(expectedMeasurements, result.returnResult().getResponseBody(), "Unexpected measurement sequence");
    }

    @Test
    void editCoasterActionChangesEntityFields() {
        var coasterName = "esp102";
        coasterRepository.create(coasterName, Instant.EPOCH, ZoneId.of("Etc/UTC")).block();
        coasterManagementService.assignCoasterToUser(coasterName, USERNAME).block();

        var before = coasterRepository.findById(coasterName).block();
        assertNull(before.getDisplayName(), "Display name is not null");
        assertNull(before.getDescription(), "Description is not null");
        assertEquals(ZoneId.of("Etc/UTC"), before.getTimezone(), "Unexpected timezone");
        assertNull(before.getPlace(), "Place is not null");

        var newDisplayName = "ESP32 coaster number 102";
        var newDescription = "Description";
        var newTimezone = ZoneId.of("Europe/Warsaw");
        var newPlace = "Mock land";

        webTestClient.mutateWith(csrf()).post()
                .uri("/user/editCoaster/" + coasterName)
                .body(fromFormData("displayName", newDisplayName)
                        .with("description", newDescription)
                        .with("timezone", newTimezone.toString())
                        .with("place", newPlace))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));

        var after = coasterRepository.findById(coasterName).block();
        assertEquals(newDisplayName, after.getDisplayName(), "Unexpected display name value");
        assertEquals(newDescription, after.getDescription(), "Unexpected description value");
        assertEquals(ZoneId.of("Europe/Warsaw"), after.getTimezone(), "Unexpected timezone");
        assertEquals(newPlace, after.getPlace(), "Unexpected place value");
    }

    @Test
    void removeCoasterActionRemovesRelationOfCoasterToUser() {
        var coasterName = "esp103";
        coasterRepository.create(coasterName, Instant.EPOCH, ZoneId.of("Etc/UTC")).block();
        coasterManagementService.assignCoasterToUser(coasterName, USERNAME).block();

        webTestClient.mutateWith(csrf()).post()
                .uri("/user/removeCoaster/" + coasterName)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));

        var after = coasterRepository.findById(coasterName).block();
        assertNull(after.getOwner(), "Coaster owner should be null");
    }
}
