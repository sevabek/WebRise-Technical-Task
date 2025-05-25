package ru.webrise.technicaltask.unit.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.webrise.technicaltask.TechnicalTaskApplication;
import ru.webrise.technicaltask.controllers.TopSubscriptionsController;
import ru.webrise.technicaltask.dto.SubscriptionStats;
import ru.webrise.technicaltask.services.SubscriptionService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TopSubscriptionsController.class)
@ContextConfiguration(classes = TechnicalTaskApplication.class)
public class TopSubscriptionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    @Test
    void getTopSubscriptions_DefaultLimit_ShouldReturn3Items() throws Exception {
        List<SubscriptionStats> mockStats = List.of(
                new SubscriptionStats("Netflix", 150L),
                new SubscriptionStats("Spotify", 120L),
                new SubscriptionStats("Disney+", 90L)
        );

        Mockito.when(subscriptionService.getTopSubscriptions(3))
                .thenReturn(mockStats);

        mockMvc.perform(get("/subscriptions/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].providerName").value("Netflix"))
                .andExpect(jsonPath("$[0].subscriptionCount").value(150));

        Mockito.verify(subscriptionService).getTopSubscriptions(3);
    }

    @Test
    void getTopSubscriptions_CustomLimit_ShouldReturnCorrectCount() throws Exception {
        List<SubscriptionStats> mockStats = List.of(
                new SubscriptionStats("Netflix", 200L),
                new SubscriptionStats("Apple Music", 150L)
        );

        Mockito.when(subscriptionService.getTopSubscriptions(2))
                .thenReturn(mockStats);

        mockMvc.perform(get("/subscriptions/top?limit=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].providerName").value("Apple Music"));

        Mockito.verify(subscriptionService).getTopSubscriptions(2);
    }

    @Test
    void getTopSubscriptions_InvalidLimit_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/subscriptions/top?limit=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(subscriptionService);
    }
}
