package ru.webrise.technicaltask.services;

import ru.webrise.technicaltask.dto.SubscriptionProviderDTO;
import ru.webrise.technicaltask.models.SubscriptionProvider;

public interface SubscriptionProviderServiceInterface {

    void saveSubscriptionProvider(SubscriptionProviderDTO providerDTO);

    void updateSubscriptionProvider(long id, SubscriptionProviderDTO providerDTO);

    void deleteSubscriptionProvider(long id);

    SubscriptionProvider getSubscriptionProviderInfo(Long id);
}
