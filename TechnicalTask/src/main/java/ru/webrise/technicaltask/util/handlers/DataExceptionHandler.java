package ru.webrise.technicaltask.util.handlers;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.webrise.technicaltask.util.exceptions.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class DataExceptionHandler {

    @ExceptionHandler({
            NonUniqueEmailException.class, NonUniqueUsernameException.class,
            NonUniqueProviderNameException.class, NonUniqueUserAndSubscriptionProvider.class
    })
    @ResponseBody
    public ResponseEntity<DataErrorResponse> handleNotUniqueDataException(RuntimeException exception) {
        DataErrorResponse errorResponse = new DataErrorResponse(
                exception.getMessage(),
                exception.getCause(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            SubscriptionNotFoundException.class, SubscriptionProviderNotFoundException.class,
            UserNotFoundException.class
    })
    @ResponseBody
    public ResponseEntity<DataErrorResponse> handleNotFoundDataException(RuntimeException exception) {
        DataErrorResponse errorResponse = new DataErrorResponse(
                exception.getMessage(),
                exception.getCause(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseBody
    private ResponseEntity<DataErrorResponse> userValidationExceptionHandler(ValidationException exception) {
        DataErrorResponse errorResponse = new DataErrorResponse(
                exception.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
