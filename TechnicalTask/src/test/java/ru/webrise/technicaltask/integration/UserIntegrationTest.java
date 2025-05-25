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
public class UserIntegrationTest {

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
    void createUser_ShouldReturnOk() throws Exception {
        String userJson = """
            {
              "username": "john_doe14",
              "email": "john.doe77@example.com",
              "fullName": "John Doe",
              "createdAt": "2024-02-20T10:00:00",
              "subscriptions": [
                  {
                      "startDate": "2024-02-20T10:00:00",
                      "endDate": "2024-02-20T10:00:00",
                      "active": true,
                      "subscriptionProvider": {
                          "id": 3
                      }
                  }
              ]
            }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @Order(2)
    void createUser_DuplicateUsername_ShouldReturnBadRequest() throws Exception {
        String duplicateUserJson = """
            {
              "username": "john_doe14",
              "email": "john.doe77@example.com",
              "fullName": "John Doe",
              "createdAt": "2024-02-20T10:00:00",
              "subscriptions": [
                  {
                      "startDate": "2024-02-20T10:00:00",
                      "endDate": "2024-02-20T10:00:00",
                      "active": true,
                      "subscriptionProvider": {
                          "id": 3
                      }
                  }
              ]
            }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateUserJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void getUser_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.email").value("user1@example.com"));
    }

    @Test
    @Order(4)
    void getUser_NotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/users/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    void updateUser_ShouldReturnOk() throws Exception {
        String updateJson = """
            {
              "username": "john_doe3",
              "email": "john.doe3@3example.com",
              "fullName": "John Doe"
            }
        """;

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User was successfully updated"));
    }

    @Test
    void deleteUser_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User was successfully deleted"));
    }
}
