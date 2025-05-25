package ru.webrise.technicaltask.integration;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.webrise.technicaltask.TechnicalTaskApplication;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(
        properties = {
                "spring.liquibase.enabled=false",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.profiles.active="
        },
        classes = TechnicalTaskApplication.class
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubscriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withInitScript("db/testcontainers-migration/postgresql/schema.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @Order(1)
    void addSubscription_ShouldReturnOk() throws Exception {
        String subscriptionJson = """
            {
                "startDate": "2024-02-20T10:00:00",
                "endDate": "2024-05-20T10:00:00",
                "active": true,
                "subscriptionProvider": {
                    "id": 3
                }
            }
        """;

        mockMvc.perform(post("/users/2/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subscriptionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    @Order(2)
    void addSubscription_InvalidData_ShouldReturnBadRequest() throws Exception {
        String invalidSubscriptionJson = """
            {
                "startDate": "invalid-date",
                "endDate": "2024-05-20T10:00:00",
                "active": true,
                "subscriptionProvider": {
                    "id": 1
                }
            }
        """;

        mockMvc.perform(post("/users/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidSubscriptionJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void updateSubscription_ShouldReturnOk() throws Exception {
        String updateJson = """
            {
                "startDate": "2024-02-20T10:00:00",
                "endDate": "2024-06-20T10:00:00",
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
    }

    @Test
    @Order(4)
    void getSubscriptions_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/users/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].startDate").exists());
    }

    @Test
    @Order(5)
    void deleteSubscription_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/users/1/subscriptions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription was successfully deleted"));
    }

    @Test
    @Order(6)
    void deleteSubscription_NotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/users/1/subscriptions/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}