package ru.webrise.technicaltask.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.webrise.technicaltask.dto.SubscriptionDTO;
import ru.webrise.technicaltask.dto.UserSubscriptionsDTO;
import ru.webrise.technicaltask.services.SubscriptionService;
import ru.webrise.technicaltask.util.handlers.BindingResultErrorHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST контроллер для управления подписками пользователей.
 * Предоставляет эндпоинты для добавления, просмотра и удаления подписок пользователя.
 */
@RestController
@RequestMapping("/users/{userId}/subscriptions")
@Validated
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final BindingResultErrorHandler bindingResultErrorHandler;

    /**
     * Добавляет новую подписку для указанного2 пользователя.
     *
     * @param userId Идентификатор пользователя
     * @param subscriptionDTO DTO с данными новой подписки
     * @param bindingResult Результат валидации входящих данных
     * @return Сообщение об успешном добавлении подписки
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> addSubscription(
            @PathVariable long userId,
            @RequestBody @Valid SubscriptionDTO subscriptionDTO,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors())
            bindingResultErrorHandler.handleError(bindingResult);

        long id = subscriptionService.saveSubscription(userId, subscriptionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("id", String.valueOf(id));
        response.put("message", "Subscription was successfully added");
        return ResponseEntity.ok(response);
    }


    /**
     * Обновляет данные о подписке пользователя.
     *
     * @param userId Идентификатор пользователя
     * @param subId Идентификатор подписки
     * @param subscriptionDTO DTO с данными обновленной подписки
     * @param bindingResult Результат валидации входящих данных
     * @return Сообщение об успешном обновлении подписки
     */
    @PutMapping("/{subId}")
    public ResponseEntity<String> updateSubscription(
            @PathVariable long userId,
            @PathVariable long subId,
            @RequestBody @Valid SubscriptionDTO subscriptionDTO,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors())
            bindingResultErrorHandler.handleError(bindingResult);

        subscriptionService.updateSubscription(userId, subId, subscriptionDTO);
        return ResponseEntity.ok("Subscription was successfully updated");
    }

    /**
     * Получает список всех подписок пользователя.
     *
     * @param userId Идентификатор пользователя
     * @return Список подписок пользователя в формате JSON
     */
    @GetMapping
    public ResponseEntity<List<UserSubscriptionsDTO>> getSubscriptions(@PathVariable @Min(1) long userId) {
        List<UserSubscriptionsDTO> subscriptions = subscriptionService.getSubscriptionByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Удаляет конкретную подписку пользователя.
     *
     * @param userId Идентификатор пользователя
     * @param subId Идентификатор удаляемой подписки
     * @return Сообщение об успешном удалении подписки
     */
    @DeleteMapping("/{subId}")
    public ResponseEntity<String> deleteSubscription(
            @PathVariable(name = "userId") long userId,
            @PathVariable(name = "subId") long subId
    ) {
        subscriptionService.deleteSubscription(subId, userId);
        return ResponseEntity.ok("Subscription was successfully deleted");
    }
}
