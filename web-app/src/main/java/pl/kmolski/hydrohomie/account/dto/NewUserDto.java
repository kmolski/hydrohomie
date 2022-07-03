package pl.kmolski.hydrohomie.account.dto;

import lombok.Data;
import pl.kmolski.hydrohomie.account.validator.MatchingPasswords;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@MatchingPasswords
public class NewUserDto implements WithPasswordConfirmation {

    @NotBlank(message = "The username cannot be blank")
    @Size(max = 63, message = "The username must be at most 63 characters long")
    private String username;

    @NotBlank(message = "The password cannot be blank")
    @Size(min = 10, message = "The password must be at least 10 characters long")
    private String password;
    private String pwConfirm;

    private boolean enabled;
}
