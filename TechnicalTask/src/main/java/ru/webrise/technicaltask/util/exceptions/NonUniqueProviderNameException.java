package ru.webrise.technicaltask.util.exceptions;

public class NonUniqueProviderNameException extends RuntimeException {

    public NonUniqueProviderNameException(String message) {
        super(message);
    }

    public NonUniqueProviderNameException() {}

    public NonUniqueProviderNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonUniqueProviderNameException(Throwable cause) {
        super(cause);
    }
}
