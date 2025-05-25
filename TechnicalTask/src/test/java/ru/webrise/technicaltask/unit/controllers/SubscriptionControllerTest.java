package ru.webrise.technicaltask.unit.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.webrise.technicaltask.TechnicalTaskApplication;
import ru.webrise.technicaltask.controllers.SubscriptionController;
import ru.webrise.technicaltask.dto.SubscriptionDTO;
import ru.webrise.technicaltask.dto.UserSubscriptionsDTO;
import ru.webrise.technicaltask.models.SubscriptionProvider;
import ru.webrise.technicaltask.services.SubscriptionService;
import ru.webrise.technicaltask.util.exceptions.SubscriptionNotFoundException;
import ru.webrise.technicaltask.util.handlers.BindingResultErrorHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@ContextConfiguration(classes = TechnicalTaskApplication.class)
public class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private BindingResultErrorHandler bindingResultErrorHandler;

    @Test
    void addSubscription_ShouldReturnSuccess() throws Exception {
        String subscriptionJson = """
            {
                "startDate": "2023-01-01T00:00:00",
                "endDate": "2023-12-31T00:00:00",
                "active": true,
                "subscriptionProvider": {
                    "id": 1
                }
            }
            """;

        Mockito.when(subscriptionService.saveSubscription(Mockito.anyLong(), Mockito.any(SubscriptionDTO.class)))
                .thenReturn(1L);

        mockMvc.perform(post("/users/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subscriptionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.message").value("Subscription was successfully added"));

        Mockito.verify(subscriptionService).saveSubscription(Mockito.eq(1L), Mockito.any(SubscriptionDTO.class));
    }

    @Test
    void addSubscription_InvalidData_ShouldReturnBadRequest() throws Exception {
        String invalidSubscriptionJson = """
            {
                "startDate": "invalid-date",
                "endDate": "2023-12-31T00:00:00",
                "active": true,
                "subscriptionProvider": {
                    "id": 1
                }
            }
            """;

        Mockito.doAnswer(invocation -> {
            BindingResult result = invocation.getArgument(0);
            if (result.hasErrors()) {
                throw new MethodArgumentNotValidException(null, result);
            }
            return null;
        }).when(bindingResultErrorHandler).handleError(Mockito.any(BindingResult.class));

        mockMvc.perform(post("/users/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidSubscriptionJson))
                .andExpect(status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).saveSubscription(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void updateSubscription_ShouldReturnSuccess() throws Exception {
        String updateJson = """
            {
                "startDate": "2023-01-01T00:00:00",
                "endDate": "2024-12-31T00:00:00",
                "active": false,
                "subscriptionProvider": {
                    "id": 2
                }
            }
            """;

        mockMvc.perform(put("/users/1/subscriptions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription was successfully updated"));

        Mockito.verify(subscriptionService).updateSubscription(Mockito.eq(1L), Mockito.eq(1L), Mockito.any(SubscriptionDTO.class));
    }

    @Test
    void getSubscriptions_ShouldReturnList() throws Exception {
        // Подготовка мок-данных
        UserSubscriptionsDTO subscription1 = new UserSubscriptionsDTO(1L, LocalDateTime.now(), LocalDateTime.now(), true, 1L, SubscriptionProvider.builder().id(1L).build());
        UserSubscriptionsDTO subscription2 = new UserSubscriptionsDTO(2L, LocalDateTime.now(), LocalDateTime.now(), true, 1L, SubscriptionProvider.builder().id(2L).build());
        List<UserSubscriptionsDTO> subscriptions = List.of(subscription1, subscription2);

        Mockito.when(subscriptionService.getSubscriptionByUserId(1L))
                .thenReturn(subscriptions);

        mockMvc.perform(get("/users/1/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].subscriptionProvider.id").value("1"))
                .andExpect(jsonPath("$[1].subscriptionProvider.id").value("2"));
    }

    @Test
    void deleteSubscription_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/users/1/subscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription was successfully deleted"));

        Mockito.verify(subscriptionService).deleteSubscription(1L, 1L);
    }
}
