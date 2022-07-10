package pl.kmolski.hydrohomie.account.dto;

public interface WithPasswordConfirmation {

    PlaintextPassword getPassword();
    PlaintextPassword getPwConfirm();
}
