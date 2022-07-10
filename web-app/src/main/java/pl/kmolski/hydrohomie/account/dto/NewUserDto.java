package pl.kmolski.hydrohomie.account.dto;

import lombok.Data;
import pl.kmolski.hydrohomie.account.validator.MatchingPasswords;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@MatchingPasswords
public class NewUserDto implements WithPasswordConfirmation {

    @NotBlank(message = "The username cannot be blank")
    @Size(max = 63, message = "The username must be at most 63 characters long")
    @Pattern(regexp = "^(\\w+)$", message = "The username can only contain letters, numbers and '_' separators")
    private final String username;

    private final PlaintextPassword password;
    private final PlaintextPassword pwConfirm;

    private final boolean enabled;
}
