package pl.kmolski.hydrohomie.account.validator;

import pl.kmolski.hydrohomie.account.dto.WithPasswordConfirmation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * <p>
 *     The annotated {@link WithPasswordConfirmation} must have
 *     a matching plaintext password and password confirmation.
 * </p>
 * <p>Accepts non-null {@link WithPasswordConfirmation}.</p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatchingPasswordsValidator.class)
public @interface MatchingPasswords {

    /**
     * @return the error message
     */
    String message() default "The passwords do not match";

    /**
     * @return the groups the constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * @return the payload associated to the constraint
     */
    Class<? extends Payload>[] payload() default {};
}
