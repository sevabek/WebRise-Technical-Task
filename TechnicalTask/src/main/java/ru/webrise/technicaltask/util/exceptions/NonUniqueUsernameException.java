package ru.webrise.technicaltask.util.exceptions;

public class NonUniqueUsernameException extends RuntimeException {

    public NonUniqueUsernameException(String message) {
        super(message);
    }

    public NonUniqueUsernameException() {}

    public NonUniqueUsernameException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonUniqueUsernameException(Throwable cause) {
        super(cause);
    }
}
