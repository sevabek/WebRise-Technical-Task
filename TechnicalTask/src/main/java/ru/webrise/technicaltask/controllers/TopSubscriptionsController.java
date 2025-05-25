package ru.webrise.technicaltask.controllers;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.webrise.technicaltask.dto.SubscriptionStats;
import ru.webrise.technicaltask.services.SubscriptionService;
import ru.webrise.technicaltask.util.handlers.BindingResultErrorHandler;

import java.util.List;

/**
 * REST контроллер для получения топа подписок.
 * Предоставляет эндпоинт для получения списка самых популярных подписок.
 */
@RestController
@RequestMapping("/subscriptions")
@Validated
@RequiredArgsConstructor
public class TopSubscriptionsController {

    private final SubscriptionService subscriptionService;

    /**
     * Возвращает список самых популярных подписок.
     *
     * @param limit Ограничение количества сервисов в списке
     * @return Список подписок
     */
    @GetMapping("/top")
    public ResponseEntity<List<SubscriptionStats>> getTopSubscriptions(
            @RequestParam(name = "limit", required = false, defaultValue = "3")
            @Min(value = 1, message = "Limit cannot be less than 0") int limit
    ) {
        List<SubscriptionStats> topProviders = subscriptionService.getTopSubscriptions(limit);
        return ResponseEntity.ok(topProviders);
    }
}