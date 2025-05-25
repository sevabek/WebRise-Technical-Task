package ru.webrise.technicaltask.services;

import ru.webrise.technicaltask.dto.SubscriptionDTO;
import ru.webrise.technicaltask.dto.SubscriptionStats;
import ru.webrise.technicaltask.dto.UserSubscriptionsDTO;

import java.util.List;

public interface SubscriptionServiceInterface {

    long saveSubscription(long userId, SubscriptionDTO subscriptionDTO);

    void updateSubscription(long userId, long subId, SubscriptionDTO subscriptionDTO);

    void deleteSubscription(long id, long userId);

    List<UserSubscriptionsDTO> getSubscriptionByUserId(long userId);

    List<SubscriptionStats> getTopSubscriptions(int limit);
}
