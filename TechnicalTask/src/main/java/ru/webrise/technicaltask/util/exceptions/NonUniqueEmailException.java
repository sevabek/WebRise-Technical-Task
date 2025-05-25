package ru.webrise.technicaltask.util.exceptions;

public class NonUniqueEmailException extends RuntimeException {

    public NonUniqueEmailException(String message) {
        super(message);
    }

    public NonUniqueEmailException() {}

    public NonUniqueEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonUniqueEmailException(Throwable cause) {
        super(cause);
    }
}
