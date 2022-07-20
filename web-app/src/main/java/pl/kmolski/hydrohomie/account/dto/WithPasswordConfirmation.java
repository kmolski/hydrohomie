package pl.kmolski.hydrohomie.account.dto;

import pl.kmolski.hydrohomie.account.validator.MatchingPasswords;

/**
 * Common interface for DTOs that contain passwords and password confirmations.
 * Compatible with the {@link MatchingPasswords} annotation and validator.
 */
public interface WithPasswordConfirmation {

    /**
     * Return the plaintext password.
     *
     * @return the plaintext password
     */
    PlaintextPassword getPassword();

    /**
     * Return the plaintext password confirmation.
     *
     * @return the plaintext password confirmation
     */
    PlaintextPassword getPwConfirm();
}
