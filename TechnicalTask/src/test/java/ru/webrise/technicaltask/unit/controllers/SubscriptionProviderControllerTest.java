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
import ru.webrise.technicaltask.controllers.SubscriptionProviderController;
import ru.webrise.technicaltask.dto.SubscriptionProviderDTO;
import ru.webrise.technicaltask.models.SubscriptionProvider;
import ru.webrise.technicaltask.services.SubscriptionProviderService;
import ru.webrise.technicaltask.util.handlers.BindingResultErrorHandler;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionProviderController.class)
@ContextConfiguration(classes = TechnicalTaskApplication.class)
public class SubscriptionProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionProviderService subscriptionProviderService;

    @MockBean
    private BindingResultErrorHandler bindingResultErrorHandler;

    @Test
    void getSubscriptionProviderById_ShouldReturnProvider() throws Exception {
        SubscriptionProvider mockProvider = new SubscriptionProvider();
        mockProvider.setId(1L);
        mockProvider.setName("Netflix");
        mockProvider.setPrice(new BigDecimal("9.99"));

        Mockito.when(subscriptionProviderService.getSubscriptionProviderInfo(1L))
                .thenReturn(mockProvider);

        mockMvc.perform(get("/subscription-provider/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Netflix"))
                .andExpect(jsonPath("$.price").value(9.99));
    }

    @Test
    void addSubscriptionProvider_ShouldReturnSuccess() throws Exception {
        String providerJson = """
            {
                "name": "New Streaming",
                "price": 12.99
            }
            """;

        mockMvc.perform(post("/subscription-provider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(providerJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription provider was successfully added"));

        Mockito.verify(subscriptionProviderService).saveSubscriptionProvider(Mockito.any(SubscriptionProviderDTO.class));
    }

    @Test
    void addSubscriptionProvider_InvalidData_ShouldReturnBadRequest() throws Exception {
        String invalidProviderJson = """
            {
                "name": "",
                "price": -1.00
            }
            """;

        Mockito.doAnswer(invocation -> {
            BindingResult result = invocation.getArgument(0);
            if (result.hasErrors()) {
                throw new MethodArgumentNotValidException(null, result);
            }
            return null;
        }).when(bindingResultErrorHandler).handleError(Mockito.any(BindingResult.class));

        mockMvc.perform(post("/subscription-provider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidProviderJson))
                .andExpect(status().isBadRequest());

        Mockito.verify(bindingResultErrorHandler, Mockito.times(1))
                .handleError(Mockito.any(BindingResult.class));
    }

    @Test
    void updateSubscriptionProvider_ShouldReturnSuccess() throws Exception {
        String updateJson = """
            {
                "name": "Updated Netflix",
                "price": 15.99
            }
            """;

        mockMvc.perform(put("/subscription-provider/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription provider was successfully updated"));

        Mockito.verify(subscriptionProviderService).updateSubscriptionProvider(
                Mockito.eq(1L), Mockito.any(SubscriptionProviderDTO.class));
    }

    @Test
    void deleteSubscriptionProvider_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/subscription-provider/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription provider was successfully deleted"));

        Mockito.verify(subscriptionProviderService).deleteSubscriptionProvider(1L);
    }
}