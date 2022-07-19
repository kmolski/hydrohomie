package pl.kmolski.hydrohomie.account.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Wrapper record for plaintext passwords.
 *
 * @param plaintext the plaintext password
 */
public record PlaintextPassword(
        @NotBlank(message = "The password cannot be blank")
        @Size(min = 10, message = "The password must be at least 10 characters long")
        String plaintext
) {
}
