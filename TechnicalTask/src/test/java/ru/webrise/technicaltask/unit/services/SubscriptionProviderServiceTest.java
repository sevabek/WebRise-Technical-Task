package ru.webrise.technicaltask.unit.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.webrise.technicaltask.dto.SubscriptionProviderDTO;
import ru.webrise.technicaltask.models.SubscriptionProvider;
import ru.webrise.technicaltask.repositories.SubscriptionProviderRepository;
import ru.webrise.technicaltask.services.SubscriptionProviderService;
import ru.webrise.technicaltask.util.exceptions.NonUniqueProviderNameException;
import ru.webrise.technicaltask.util.exceptions.SubscriptionProviderNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionProviderServiceTest {

    @Mock
    private SubscriptionProviderRepository subscriptionProviderRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SubscriptionProviderService subscriptionProviderService;

    @Test
    @DisplayName("Создание провайдера - успешный сценарий")
    void saveSubscriptionProvider_ShouldSaveSuccessfully() {
        SubscriptionProviderDTO providerDTO = new SubscriptionProviderDTO();
        providerDTO.setName("New Provider");
        providerDTO.setPrice(new BigDecimal("9.99"));

        SubscriptionProvider provider = new SubscriptionProvider();
        provider.setId(1L);

        when(subscriptionProviderRepository.existsByName("New Provider")).thenReturn(false);
        when(modelMapper.map(providerDTO, SubscriptionProvider.class)).thenReturn(provider);
        when(subscriptionProviderRepository.save(provider)).thenReturn(provider);

        subscriptionProviderService.saveSubscriptionProvider(providerDTO);

        verify(subscriptionProviderRepository).save(provider);
    }

    @Test
    @DisplayName("Создание провайдера - неуникальное имя")
    void saveSubscriptionProvider_NonUniqueName_ShouldThrowException() {
        SubscriptionProviderDTO providerDTO = new SubscriptionProviderDTO();
        providerDTO.setName("Existing Provider");

        when(subscriptionProviderRepository.existsByName("Existing Provider")).thenReturn(true);

        assertThrows(NonUniqueProviderNameException.class,
                () -> subscriptionProviderService.saveSubscriptionProvider(providerDTO));
        verify(subscriptionProviderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление провайдера - успешный сценарий")
    void updateSubscriptionProvider_ShouldUpdateSuccessfully() {
        long providerId = 1L;
        SubscriptionProviderDTO providerDTO = new SubscriptionProviderDTO("Updated Provider", new BigDecimal("12.99"));

        SubscriptionProvider existingProvider = new SubscriptionProvider();
        existingProvider.setId(providerId);

        when(subscriptionProviderRepository.findById(providerId))
                .thenReturn(Optional.of(existingProvider));

        subscriptionProviderService.updateSubscriptionProvider(providerId, providerDTO);

        verify(modelMapper).map(providerDTO, existingProvider);
        verify(subscriptionProviderRepository).save(existingProvider);
    }

    @Test
    @DisplayName("Обновление провайдера - провайдер не найден")
    void updateSubscriptionProvider_NotFound_ShouldThrowException() {
        long nonExistingId = 999L;
        SubscriptionProviderDTO providerDTO = new SubscriptionProviderDTO();

        when(subscriptionProviderRepository.findById(nonExistingId))
                .thenReturn(Optional.empty());

        assertThrows(SubscriptionProviderNotFoundException.class,
                () -> subscriptionProviderService.updateSubscriptionProvider(nonExistingId, providerDTO));
        verify(subscriptionProviderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление провайдера - успешный сценарий")
    void deleteSubscriptionProvider_ShouldDeleteSuccessfully() {
        long providerId = 1L;
        SubscriptionProvider provider = new SubscriptionProvider();
        provider.setId(providerId);

        when(subscriptionProviderRepository.findById(providerId))
                .thenReturn(Optional.of(provider));

        subscriptionProviderService.deleteSubscriptionProvider(providerId);

        verify(subscriptionProviderRepository).delete(provider);
    }

    @Test
    @DisplayName("Получение информации о провайдере - успешный сценарий")
    void getSubscriptionProviderInfo_ShouldReturnProvider() {
        long providerId = 1L;
        SubscriptionProvider provider = new SubscriptionProvider();
        provider.setId(providerId);
        provider.setName("Test Provider");

        when(subscriptionProviderRepository.findById(providerId))
                .thenReturn(Optional.of(provider));

        SubscriptionProvider result = subscriptionProviderService.getSubscriptionProviderInfo(providerId);

        assertEquals("Test Provider", result.getName());
    }
}
