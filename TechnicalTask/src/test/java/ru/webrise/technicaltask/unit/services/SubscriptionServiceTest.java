package ru.webrise.technicaltask.unit.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import ru.webrise.technicaltask.dto.SubscriptionDTO;
import ru.webrise.technicaltask.dto.SubscriptionStats;
import ru.webrise.technicaltask.dto.UserSubscriptionsDTO;
import ru.webrise.technicaltask.models.Subscription;
import ru.webrise.technicaltask.models.SubscriptionProvider;
import ru.webrise.technicaltask.models.User;
import ru.webrise.technicaltask.repositories.SubscriptionRepository;
import ru.webrise.technicaltask.services.SubscriptionProviderService;
import ru.webrise.technicaltask.services.SubscriptionService;
import ru.webrise.technicaltask.services.UserService;
import ru.webrise.technicaltask.util.exceptions.NonUniqueUserAndSubscriptionProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserService userService;

    @Mock
    private SubscriptionProviderService subscriptionProviderService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    @DisplayName("Создание подписки - успешный сценарий")
    void saveSubscription_ShouldReturnSubscriptionId() {
        long userId = 1L;
        long providerId = 2L;
        long subscriptionId = 3L;

        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionProvider(new SubscriptionProvider(providerId, null, null));

        User mockUser = new User();
        mockUser.setId(userId);

        Subscription mockSubscription = new Subscription();
        mockSubscription.setId(subscriptionId);
        mockSubscription.setUser(mockUser);

        when(userService.getUserInfo(userId)).thenReturn(mockUser);
        when(subscriptionProviderService.getSubscriptionProviderInfo(providerId))
                .thenReturn(new SubscriptionProvider());
        when(subscriptionRepository.existsBySubscriptionProvider_IdAndUser_Id(providerId, userId))
                .thenReturn(false);
        when(modelMapper.map(subscriptionDTO, Subscription.class)).thenReturn(mockSubscription);
        when(subscriptionRepository.save(mockSubscription)).thenReturn(mockSubscription);

        long result = subscriptionService.saveSubscription(userId, subscriptionDTO);

        assertEquals(subscriptionId, result);
        verify(subscriptionRepository).save(mockSubscription);
    }

    @Test
    @DisplayName("Создание подписки - дубликат подписки")
    void saveSubscription_DuplicateSubscription_ShouldThrowException() {
        long userId = 1L;
        long providerId = 2L;
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionProvider(new SubscriptionProvider(providerId, null, null));

        when(userService.getUserInfo(userId)).thenReturn(new User());
        when(subscriptionProviderService.getSubscriptionProviderInfo(providerId))
                .thenReturn(new SubscriptionProvider());
        when(subscriptionRepository.existsBySubscriptionProvider_IdAndUser_Id(providerId, userId))
                .thenReturn(true);

        assertThrows(NonUniqueUserAndSubscriptionProvider.class,
                () -> subscriptionService.saveSubscription(userId, subscriptionDTO));
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление подписки - успешный сценарий")
    void updateSubscription_ShouldUpdateSuccessfully() {
        long userId = 1L;
        long subId = 2L;
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();

        Subscription existingSubscription = new Subscription();
        existingSubscription.setId(subId);
        existingSubscription.setUser(new User(userId, null, null, null, null, null, null));

        when(userService.getUserInfo(userId)).thenReturn(new User());
        when(subscriptionRepository.findByIdAndUser_Id(subId, userId))
                .thenReturn(Optional.of(existingSubscription));

        subscriptionService.updateSubscription(userId, subId, subscriptionDTO);

        verify(modelMapper).map(subscriptionDTO, existingSubscription);
        verify(subscriptionRepository).save(existingSubscription);
    }

    @Test
    @DisplayName("Удаление подписки - успешный сценарий")
    void deleteSubscription_ShouldDeleteSuccessfully() {
        long userId = 1L;
        long subId = 2L;
        Subscription subscription = new Subscription();
        subscription.setId(subId);
        subscription.setUser(new User(userId, null, null, null, null, null, null));

        when(subscriptionRepository.findByIdAndUser_Id(subId, userId))
                .thenReturn(Optional.of(subscription));

        subscriptionService.deleteSubscription(subId, userId);

        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    @DisplayName("Получение подписок пользователя - успешный сценарий")
    void getSubscriptionByUserId_ShouldReturnSubscriptions() {
        long userId = 1L;
        List<UserSubscriptionsDTO> mockSubscriptions = List.of(
                new UserSubscriptionsDTO(1L, LocalDateTime.now(), LocalDateTime.now(), true, 1L, SubscriptionProvider.builder().id(1L).build()),
                new UserSubscriptionsDTO(2L, LocalDateTime.now(), LocalDateTime.now(), true, 1L, SubscriptionProvider.builder().id(2L).build())
        );

        when(subscriptionRepository.findByUserId(userId)).thenReturn(mockSubscriptions);

        List<UserSubscriptionsDTO> result = subscriptionService.getSubscriptionByUserId(userId);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).subscriptionProvider().getId());
    }

    @Test
    @DisplayName("Получение топ подписок - успешный сценарий")
    void getTopSubscriptions_ShouldReturnTopSubscriptions() {
        int limit = 3;
        List<SubscriptionStats> mockStats = List.of(
                new SubscriptionStats("Netflix", 100L),
                new SubscriptionStats("Spotify", 80L),
                new SubscriptionStats("Disney+", 50L)
        );

        when(subscriptionRepository.findTopBySubscriptions(PageRequest.of(0, limit)))
                .thenReturn(mockStats);

        List<SubscriptionStats> result = subscriptionService.getTopSubscriptions(limit);

        assertEquals(3, result.size());
        assertEquals("Netflix", result.get(0).providerName());
        assertEquals(100, result.get(0).subscriptionCount());
    }
}
