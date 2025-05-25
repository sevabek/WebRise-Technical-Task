package ru.webrise.technicaltask.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import ru.webrise.technicaltask.dto.SubscriptionProviderDTO;
import ru.webrise.technicaltask.models.SubscriptionProvider;
import ru.webrise.technicaltask.repositories.SubscriptionProviderRepository;
import ru.webrise.technicaltask.util.exceptions.NonUniqueProviderNameException;
import ru.webrise.technicaltask.util.exceptions.SubscriptionProviderNotFoundException;
import ru.webrise.technicaltask.controllers.SubscriptionProviderController;

/**
 * Сервис для работы с провайдерами подписок.
 * <p>
 * Обеспечивает бизнес-логику управления провайдерами подписочных сервисов,
 * включая создание, обновление, удаление и получение информации о провайдерах.
 * <p>
 * Используется в контроллерах:
 * <ul>
 *     <li>{@link SubscriptionProviderController} - для всех операций с провайдерами подписок</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class SubscriptionProviderService implements SubscriptionProviderServiceInterface {

    private final SubscriptionProviderRepository subscriptionProviderRepository;
    private final ModelMapper modelMapper;

    /**
     * Создает нового провайдера подписок.
     * <p>
     * Используется в {@link SubscriptionProviderController#addSubscriptionProvider(SubscriptionProviderDTO, BindingResult)}
     * при добавлении нового провайдера.
     * </p>
     *
     * @param providerDTO DTO с данными провайдера
     * @throws NonUniqueProviderNameException если провайдер с таким именем уже существует
     */
    @Override
    @Transactional
    public void saveSubscriptionProvider(SubscriptionProviderDTO providerDTO) {
        log.info("Attempting to save new subscription provider with name: {}", providerDTO.getName());

        if (subscriptionProviderRepository.existsByName(providerDTO.getName())) {
            log.error("Failed to save provider: provider with name '{}' already exists", providerDTO.getName());
            throw new NonUniqueProviderNameException("This provider name is already taken");
        }

        SubscriptionProvider provider = convertToSubscriptionProvider(providerDTO);
        provider = subscriptionProviderRepository.save(provider);

        log.info("Successfully saved new subscription provider with ID: {}", provider.getId());
    }

    /**
     * Обновляет данные существующего провайдера подписок.
     * <p>
     * Используется в {@link SubscriptionProviderController#updateSubscriptionProvider(long, SubscriptionProviderDTO, BindingResult)}
     * при изменении данных провайдера.
     * </p>
     *
     * @param id ID провайдера
     * @param providerDTO DTO с обновляемыми данными провайдера
     * @throws SubscriptionProviderNotFoundException если провайдер не найден
     */
    @Override
    @Transactional
    public void updateSubscriptionProvider(long id, SubscriptionProviderDTO providerDTO) {
        log.info("Attempting to update subscription provider with ID: {}", id);

        SubscriptionProvider provider = subscriptionProviderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Failed to update provider: provider with ID {} not found", id);
                    return new SubscriptionProviderNotFoundException("SubscriptionProvider with that id was not found");
                });

        convertToSubscriptionProvider(providerDTO, provider);
        subscriptionProviderRepository.save(provider);

        log.info("Successfully updated subscription provider with ID: {}", id);
    }

    /**
     * Удаляет провайдера подписок.
     * <p>
     * Используется в {@link SubscriptionProviderController#deleteSubscriptionProvider(long)}
     * при удалении провайдера.
     * </p>
     *
     * @param id ID удаляемого провайдера
     * @throws SubscriptionProviderNotFoundException если провайдер не найден
     */
    @Override
    @Transactional
    public void deleteSubscriptionProvider(long id) {
        log.info("Attempting to delete subscription provider with ID: {}", id);

        SubscriptionProvider provider = subscriptionProviderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Failed to delete provider: provider with ID {} not found", id);
                    return new SubscriptionProviderNotFoundException("SubscriptionProvider with that id was not found");
                });

        subscriptionProviderRepository.delete(provider);

        log.info("Successfully deleted subscription provider with ID: {}", id);
    }

    /**
     * Получает информацию о провайдере подписок.
     * <p>
     * Используется в {@link SubscriptionProviderController#getSubscriptionProviderById(long)}
     * для получения данных провайдера.
     * </p>
     *
     * @param id ID провайдера
     * @return Сущность провайдера подписок
     * @throws SubscriptionProviderNotFoundException если провайдер не найден
     */
    @Override
    public SubscriptionProvider getSubscriptionProviderInfo(Long id) {
        log.debug("Fetching subscription provider info for ID: {}", id);
        return subscriptionProviderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Failed to fetch provider: provider with ID {} not found", id);
                    return new SubscriptionProviderNotFoundException("SubscriptionProvider with that id was not found");
                });
    }

    /**
     * Конвертирует DTO в сущность SubscriptionProvider.
     *
     * @param dto DTO провайдера подписок
     * @return Сущность SubscriptionProvider
     */
    private SubscriptionProvider convertToSubscriptionProvider(SubscriptionProviderDTO dto) {
        log.trace("Converting SubscriptionProviderDTO to entity");
        return modelMapper.map(dto, SubscriptionProvider.class);
    }

    /**
     * Обновляет сущность SubscriptionProvider данными из DTO.
     *
     * @param dto DTO провайдера подписок
     * @param provider Сущность для обновления
     */
    private void convertToSubscriptionProvider(SubscriptionProviderDTO dto, SubscriptionProvider provider) {
        log.trace("Updating SubscriptionProvider entity from DTO");
        modelMapper.map(dto, provider);
    }
}
