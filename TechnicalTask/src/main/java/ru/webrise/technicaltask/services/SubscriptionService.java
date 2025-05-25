package ru.webrise.technicaltask.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import ru.webrise.technicaltask.dto.SubscriptionDTO;
import ru.webrise.technicaltask.dto.SubscriptionStats;
import ru.webrise.technicaltask.dto.UserSubscriptionsDTO;
import ru.webrise.technicaltask.models.Subscription;
import ru.webrise.technicaltask.models.User;
import ru.webrise.technicaltask.repositories.SubscriptionRepository;
import ru.webrise.technicaltask.util.exceptions.NonUniqueUserAndSubscriptionProvider;
import ru.webrise.technicaltask.util.exceptions.SubscriptionNotFoundException;
import ru.webrise.technicaltask.controllers.*;
import ru.webrise.technicaltask.util.exceptions.UserNotFoundException;

import java.util.List;

/**
 * Сервис для управления подписками пользователей.
 * <p>
 * Обеспечивает бизнес-логику работы с подписками, включая создание, обновление,
 * удаление и получение информации о подписках.
 * <p>
 * Используется в контроллерах:
 * <ul>
 *     <li>{@link SubscriptionController} - для операций с подписками пользователей</li>
 *     <li>{@link TopSubscriptionsController} - для получения списка наиболее популярных подписок</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionServiceInterface {

    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final SubscriptionProviderService subscriptionProviderService;
    private final ModelMapper modelMapper;

    /**
     * Создает новую подписку для пользователя.
     * <p>
     * Используется в {@link SubscriptionController#addSubscription(long, SubscriptionDTO, BindingResult)}
     * при добавлении новой подписки.
     * </p>
     *
     * @param userId ID пользователя
     * @param subscriptionDTO DTO с данными подписки
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    @Transactional
    public long saveSubscription(long userId, SubscriptionDTO subscriptionDTO) {
        log.info("Attempting to save new subscription for user ID: {}", userId);

        User user = userService.getUserInfo(userId);
        subscriptionProviderService.getSubscriptionProviderInfo(subscriptionDTO.getSubscriptionProvider().getId());

        if (subscriptionRepository.existsBySubscriptionProvider_IdAndUser_Id(subscriptionDTO.getSubscriptionProvider().getId(), userId)) {
            log.error("Failed to create subscription: subscription with service_id {} and user_id {} already exists",
                    subscriptionDTO.getSubscriptionProvider().getId(), userId);
            throw new NonUniqueUserAndSubscriptionProvider("This subscription already exists");
        }

        Subscription subscription = convertToSubscription(subscriptionDTO);
        subscription.setUser(user);
        subscription = subscriptionRepository.save(subscription);

        log.info("Successfully saved subscription with ID: {} for user ID: {}",
                subscription.getId(), userId);

        return subscription.getId();
    }

    /**
     * Обновляет существующую подписку пользователя.
     * <p>
     * Используется в {@link SubscriptionController#updateSubscription(long, long, SubscriptionDTO, BindingResult)}
     * при изменении параметров подписки.
     * </p>
     *
     * @param userId ID пользователя
     * @param subId ID подписки
     * @param subscriptionDTO DTO с обновляемыми данными подписки
     * @throws UserNotFoundException если пользователь не найден
     * @throws SubscriptionNotFoundException если подписка не найдена
     */
    @Override
    @Transactional
    public void updateSubscription(long userId, long subId, SubscriptionDTO subscriptionDTO) {
        log.info("Attempting to update subscription ID: {} for user ID: {}", subId, userId);

        userService.getUserInfo(userId);
        Subscription subscription = subscriptionRepository.findByIdAndUser_Id(subId, userId)
                .orElseThrow(() -> {
                    log.error("Failed to update subscription: subscription ID: {} not found for user ID: {}", subId, userId);
                    return new SubscriptionNotFoundException("No subscriptions was found for this user");
                });

        convertToSubscription(subscriptionDTO, subscription);
        subscriptionRepository.save(subscription);

        log.info("Successfully updated subscription ID: {} for user ID: {}", subId, userId);
    }

    /**
     * Удаляет подписку пользователя.
     * <p>
     * Используется в {@link SubscriptionController#deleteSubscription(long, long)}
     * при удалении подписки.
     * </p>
     *
     * @param subId ID подписки
     * @param userId ID пользователя
     * @throws SubscriptionNotFoundException если подписка не найдена
     */
    @Override
    @Transactional
    public void deleteSubscription(long subId, long userId) {
        log.info("Attempting to delete subscription ID: {} for user ID: {}", subId, userId);

        Subscription subscription = subscriptionRepository.findByIdAndUser_Id(subId, userId)
                .orElseThrow(() -> {
                    log.error("Failed to update subscription: subscription ID: {} not found for user ID: {} during deletion", subId, userId);
                    return new SubscriptionNotFoundException("No subscriptions was found for this user");
                });

        subscriptionRepository.delete(subscription);
        log.info("Successfully deleted subscription ID: {} for user ID: {}", subId, userId);
    }

    /**
     * Получает список подписок пользователя.
     * <p>
     * Используется в {@link SubscriptionController#getSubscriptions(long)}
     * для получения всех подписок пользователя.
     * </p>
     *
     * @param userId ID пользователя
     * @return Список подписок
     * @throws SubscriptionNotFoundException если подписки не найдены
     */
    @Override
    public List<UserSubscriptionsDTO> getSubscriptionByUserId(long userId) {
        log.debug("Fetching subscriptions for user ID: {}", userId);

        List<UserSubscriptionsDTO> subscriptions = subscriptionRepository.findByUserId(userId);

        if (subscriptions.isEmpty()) {
            log.warn("No subscriptions found for user ID: {}", userId);
            throw new SubscriptionNotFoundException("No subscriptions was found for this user");
        }

        log.debug("Found {} subscriptions for user ID: {}", subscriptions.size(), userId);
        return subscriptions;
    }

    /**
     * Получает топ популярных подписок.
     * <p>
     * Используется в {@link TopSubscriptionsController#getTopSubscriptions(int)}
     * для аналитики наиболее популярных подписок.
     * </p>
     *
     * @param limit количество возвращаемых записей
     * @return Список статистики подписок
     * @throws SubscriptionNotFoundException если подписки не найдены
     */
    @Override
    public List<SubscriptionStats> getTopSubscriptions(int limit) {
        log.debug("Fetching top {} subscriptions", limit);

        List<SubscriptionStats> subscriptions = subscriptionRepository
                .findTopBySubscriptions(PageRequest.of(0, limit));

        if (subscriptions.isEmpty()) {
            log.warn("No subscriptions found in top {} request", limit);
            throw new SubscriptionNotFoundException("No subscriptions was found");
        }

        log.debug("Found {} top subscriptions", subscriptions.size());
        return subscriptions;
    }

    /**
     * Конвертирует DTO в сущность Subscription.
     *
     * @param subscriptionDTO DTO подписки
     * @return Сущность Subscription
     */
    private Subscription convertToSubscription(SubscriptionDTO subscriptionDTO) {
        log.trace("Converting SubscriptionDTO to entity");
        return modelMapper.map(subscriptionDTO, Subscription.class);
    }

    /**
     * Обновляет сущность Subscription данными из DTO.
     *
     * @param subscriptionDTO DTO подписки
     * @param subscription Сущность для обновления
     */
    private void convertToSubscription(SubscriptionDTO subscriptionDTO, Subscription subscription) {
        log.trace("Updating Subscription entity from DTO");
        modelMapper.map(subscriptionDTO, subscription);
    }
}
