package ru.webrise.technicaltask.dto;

import ru.webrise.technicaltask.models.SubscriptionProvider;

import java.time.LocalDateTime;


public record UserSubscriptionsDTO (

        Long id,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean active,
        Long userId,
        SubscriptionProvider subscriptionProvider
) {}
