package pl.kmolski.hydrohomie.account.dto;

import lombok.Data;
import pl.kmolski.hydrohomie.account.validator.MatchingPasswords;

import javax.validation.Valid;

@Data
@MatchingPasswords
public class ChangeUserPasswordDto implements WithPasswordConfirmation {

    private final @Valid PlaintextPassword password;
    private final @Valid PlaintextPassword pwConfirm;
}
