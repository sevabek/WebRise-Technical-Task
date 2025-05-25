package ru.webrise.technicaltask.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.webrise.technicaltask.dto.SubscriptionProviderDTO;
import ru.webrise.technicaltask.models.SubscriptionProvider;
import ru.webrise.technicaltask.services.SubscriptionProviderService;
import ru.webrise.technicaltask.util.handlers.BindingResultErrorHandler;

/**
 * REST контроллер для управления провайдерами подписок.
 * Предоставляет CRUD-эндпоинты для работы с поставщиками подписочных сервисов.
 */
@RestController
@RequestMapping("/subscription-provider")
@RequiredArgsConstructor
public class SubscriptionProviderController {

    private final SubscriptionProviderService subscriptionProviderService;
    private final BindingResultErrorHandler bindingResultErrorHandler;

    /**
     * Получает информацию о провайдере подписки по идентификатору.
     *
     * @param id Идентификатор провайдера подписки
     * @return Данные провайдера в формате JSON
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionProvider> getSubscriptionProviderById(@PathVariable long id) {
        SubscriptionProvider provider = subscriptionProviderService.getSubscriptionProviderInfo(id);
        return ResponseEntity.ok().body(provider);
    }

    /**
     * Создает нового провайдера подписок.
     *
     * @param subscriptionProviderDTO DTO с данными нового провайдера
     * @param bindingResult Результат валидации входящих данных
     * @return Сообщение об успешном создании провайдера
     */
    @PostMapping
    public ResponseEntity<String> addSubscriptionProvider(
            @RequestBody @Valid SubscriptionProviderDTO subscriptionProviderDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            bindingResultErrorHandler.handleError(bindingResult);

        subscriptionProviderService.saveSubscriptionProvider(subscriptionProviderDTO);
        return ResponseEntity.ok("Subscription provider was successfully added");
    }

    /**
     * Обновляет данные существующего провайдера подписок.
     *
     * @param id Идентификатор обновляемого провайдера
     * @param providerDTO DTO с обновляемыми данными провайдера
     * @param bindingResult Результат валидации входящих данных
     * @return Сообщение об успешном обновлении провайдера
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateSubscriptionProvider(
            @PathVariable long id,
            @RequestBody @Valid SubscriptionProviderDTO providerDTO,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors())
            bindingResultErrorHandler.handleError(bindingResult);

        subscriptionProviderService.updateSubscriptionProvider(id, providerDTO);
        return ResponseEntity.ok("Subscription provider was successfully updated");
    }

    /**
     * Удаляет провайдера подписок по идентификатору.
     *
     * @param id Идентификатор удаляемого провайдера
     * @return Сообщение об успешном удалении провайдера
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscriptionProvider(@PathVariable long id) {
        subscriptionProviderService.deleteSubscriptionProvider(id);
        return ResponseEntity.ok("Subscription provider was successfully deleted");
    }
}
