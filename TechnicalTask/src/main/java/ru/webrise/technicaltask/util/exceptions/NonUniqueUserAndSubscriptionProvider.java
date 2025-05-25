package ru.webrise.technicaltask.util.exceptions;

public class NonUniqueUserAndSubscriptionProvider extends RuntimeException {

    public NonUniqueUserAndSubscriptionProvider(String message) {
        super(message);
    }

    public NonUniqueUserAndSubscriptionProvider() {}

    public NonUniqueUserAndSubscriptionProvider(String message, Throwable cause) {
        super(message, cause);
    }

    public NonUniqueUserAndSubscriptionProvider(Throwable cause) {
        super(cause);
    }
}
