package ru.webrise.technicaltask.util.exceptions;

public class SubscriptionProviderNotFoundException extends RuntimeException {

    public SubscriptionProviderNotFoundException(String message) {
        super(message);
    }

    public SubscriptionProviderNotFoundException() {}

    public SubscriptionProviderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriptionProviderNotFoundException(Throwable cause) {
        super(cause);
    }
}
