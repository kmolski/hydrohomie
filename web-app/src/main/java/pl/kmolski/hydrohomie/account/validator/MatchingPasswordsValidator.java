package pl.kmolski.hydrohomie.account.validator;

import pl.kmolski.hydrohomie.account.dto.WithPasswordConfirmation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MatchingPasswordsValidator implements ConstraintValidator<MatchingPasswords, WithPasswordConfirmation> {

    @Override
    public boolean isValid(WithPasswordConfirmation value, ConstraintValidatorContext context) {
        return value.getPassword().equals(value.getPwConfirm());
    }
}
