package pl.kmolski.hydrohomie.account.dto;

import lombok.Data;
import pl.kmolski.hydrohomie.account.validator.MatchingPasswords;

import javax.validation.Valid;

/**
 * <p>
 *     Data class describing the password to be set.
 *     Contains the password and password confirmation.
 * </p>
 * <p>The password and password confirmation must be the same.</p>
 */
@Data
@MatchingPasswords
public class ChangeUserPasswordDto implements WithPasswordConfirmation {

    private final @Valid PlaintextPassword password;
    private final @Valid PlaintextPassword pwConfirm;
}
