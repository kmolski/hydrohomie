package pl.kmolski.hydrohomie.account.dto;

import lombok.Data;
import pl.kmolski.hydrohomie.account.validator.MatchingPasswords;

@Data
@MatchingPasswords
public class ChangeUserPasswordDto implements WithPasswordConfirmation {

    private final PlaintextPassword password;
    private final PlaintextPassword pwConfirm;
}
