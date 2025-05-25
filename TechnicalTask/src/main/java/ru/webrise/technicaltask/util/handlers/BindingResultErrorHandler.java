package ru.webrise.technicaltask.util.handlers;

import jakarta.validation.ValidationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.stream.Collectors;

@Component
public class BindingResultErrorHandler {
    public void handleError(BindingResult bindingResult) {
        String errorMsg = bindingResult
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        throw new ValidationException(errorMsg);
    }
}
