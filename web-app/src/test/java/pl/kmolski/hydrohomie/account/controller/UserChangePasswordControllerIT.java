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

class UserChangePasswordControllerIT extends WebFluxControllerIT {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser("james_t_kirk100")
    void changePasswordActionUpdatesPasswordColumn() {
        var username = "james_t_kirk100";
        var oldEncodedPassword = passwordEncoder.encode("enterprise");
        userRepository.create(username, oldEncodedPassword, true).block();

        var newPassword = new PlaintextPassword("troubling tribbles");
        webTestClient.mutateWith(csrf()).post().uri("/user/changePassword")
                .body(fromFormData("password", newPassword.plaintext()).with("pwConfirm", newPassword.plaintext()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Success"));

        var account = userRepository.findById(username).block();
        assertNotNull(account, "Returned user was null");
        assertNotEquals(oldEncodedPassword, account.getPassword(), "Password did not change");
        assertTrue(passwordEncoder.matches(newPassword.plaintext(), account.getPassword()), "New password is not set");
    }
}
