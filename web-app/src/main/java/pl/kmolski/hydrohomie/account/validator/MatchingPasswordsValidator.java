package pl.kmolski.hydrohomie.account.validator;

import pl.kmolski.hydrohomie.account.dto.WithPasswordConfirmation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * <p>Implementation of the {@link MatchingPasswords} validator logic.</p>
 * <p>Accepts non-null {@link WithPasswordConfirmation}.</p>
 */
public class MatchingPasswordsValidator implements ConstraintValidator<MatchingPasswords, WithPasswordConfirmation> {

    @Override
    public boolean isValid(WithPasswordConfirmation value, ConstraintValidatorContext context) {
        return value.getPassword().equals(value.getPwConfirm());
    }
}
