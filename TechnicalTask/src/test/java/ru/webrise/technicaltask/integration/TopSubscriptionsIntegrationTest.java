package ru.webrise.technicaltask.integration;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.webrise.technicaltask.TechnicalTaskApplication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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
public class TopSubscriptionsIntegrationTest {

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
    void getTopSubscriptions_DefaultLimit_ShouldReturn3Items() throws Exception {
        mockMvc.perform(get("/subscriptions/top")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].subscriptionCount").exists())
                .andExpect(jsonPath("$[0].providerName").exists());
    }

    @Test
    void getTopSubscriptions_CustomLimit_ShouldReturnCorrectCount() throws Exception {
        int testLimit = 2;
        mockMvc.perform(get("/subscriptions/top?limit={limit}", testLimit)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(testLimit)));
    }

    @Test
    void getTopSubscriptions_InvalidLimit_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/subscriptions/top?limit=0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTopSubscriptions_OrderedByPopularity() throws Exception {
        MvcResult result = mockMvc.perform(get("/subscriptions/top?limit=5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        assertThat(
                (Integer) JsonPath.read(content, "$[0].subscriptionCount"),
                greaterThanOrEqualTo(JsonPath.read(content, "$[1].subscriptionCount"))
        );
    }
}