package pl.kmolski.hydrohomie.account.dto;

import lombok.Data;
import pl.kmolski.hydrohomie.account.validator.MatchingPasswords;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * <p>
 *     Data class describing the user to be created. Contains the
 *     username, password, password confirmation and enabled status.
 * </p>
 * <p>The password and password confirmation must be the same.</p>
 */
@Data
@MatchingPasswords
public class NewUserDto implements WithPasswordConfirmation {

    @NotBlank(message = "The username cannot be blank")
    @Size(max = 63, message = "The username must be at most 63 characters long")
    @Pattern(regexp = "^(\\w+)$", message = "The username can only contain letters, numbers and '_' separators")
    private final String username;

    private final @Valid PlaintextPassword password;
    private final @Valid PlaintextPassword pwConfirm;

    private boolean enabled;
}
