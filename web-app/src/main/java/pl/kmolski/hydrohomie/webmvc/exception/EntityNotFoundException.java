package pl.kmolski.hydrohomie.webmvc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception representing a failure to fetch the desired entity.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

    /**
     * Construct a new {@link EntityNotFoundException} with the given message.
     *
     * @param message the exception message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
