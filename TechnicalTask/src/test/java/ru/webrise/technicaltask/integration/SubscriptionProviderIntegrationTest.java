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
public class SubscriptionProviderIntegrationTest {

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
    void getSubscriptionProviderById_ShouldReturnProvider() throws Exception {
        mockMvc.perform(get("/subscription-provider/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.price").exists());
    }

    @Test
    @Order(2)
    void getSubscriptionProviderById_NotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/subscription-provider/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void addSubscriptionProvider_ShouldReturnOk() throws Exception {
        String providerJson = """
            {
                "name": "New Streaming Service",
                "price": 12.99
            }
        """;

        mockMvc.perform(post("/subscription-provider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(providerJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription provider was successfully added"));
    }

    @Test
    @Order(4)
    void updateSubscriptionProvider_ShouldReturnOk() throws Exception {
        String updateJson = """
            {
                "name": "Updated Streaming Service",
                "price": 14.99
            }
        """;

        mockMvc.perform(put("/subscription-provider/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription provider was successfully updated"));
    }

    @Test
    @Order(5)
    void updateSubscriptionProvider_NotFound_ShouldReturnNotFound() throws Exception {
        String updateJson = """
            {
                "name": "Non-existent Provider",
                "price": 9.99
            }
        """;

        mockMvc.perform(put("/subscription-provider/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void deleteSubscriptionProvider_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/subscription-provider/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription provider was successfully deleted"));
    }
}
