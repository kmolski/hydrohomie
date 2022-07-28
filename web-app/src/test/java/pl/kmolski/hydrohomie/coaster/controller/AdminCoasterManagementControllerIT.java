package pl.kmolski.hydrohomie.coaster.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import pl.kmolski.hydrohomie.account.repo.UserRepository;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.testutil.WebFluxControllerIT;

import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser(roles = {"ADMIN"})
class AdminCoasterManagementControllerIT extends WebFluxControllerIT {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CoasterRepository coasterRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void assignCoasterActionChangesOwnerColumn() {
        var username = "james_t_kirk200";
        var encodedPassword = passwordEncoder.encode("enterprise");
        userRepository.create(username, encodedPassword, true).block();

        var coasterName = "esp000";
        coasterRepository.create(coasterName, Instant.EPOCH, ZoneId.of("Etc/UTC")).block();

        webTestClient.mutateWith(csrf()).post()
                .uri("/admin/coasters/assignCoaster/" + coasterName + "?userId=" + username)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));

        var coaster = coasterRepository.findById(coasterName).block();
        assertNotNull(coaster, "Coaster not found");
        assertEquals(username, coaster.getOwner(), "Coaster owner is not assigned");
    }

    @Test
    void assignCoasterActionWithNonexistentUserFails() {
        var username = "james_t_kirk201";
        var coasterName = "esp001";
        coasterRepository.create(coasterName, Instant.EPOCH, ZoneId.of("Etc/UTC")).block();

        webTestClient.mutateWith(csrf()).post()
                .uri("/admin/coasters/assignCoaster/" + coasterName + "?userId=" + username)
                .exchange()
                .expectStatus().isNotFound();

        var coaster = coasterRepository.findById(coasterName).block();
        assertNotNull(coaster, "Coaster not found");
        assertNull(coaster.getOwner(), "Coaster owner is assigned");
    }
}
