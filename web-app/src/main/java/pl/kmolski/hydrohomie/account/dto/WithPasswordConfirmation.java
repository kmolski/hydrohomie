package pl.kmolski.hydrohomie.account.dto;

import javax.validation.constraints.NotBlank;

public interface WithPasswordConfirmation {

    @NotBlank String getPassword();
    @NotBlank String getPwConfirm();
}
