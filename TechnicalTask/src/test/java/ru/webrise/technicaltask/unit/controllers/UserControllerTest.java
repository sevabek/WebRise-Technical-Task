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
import ru.webrise.technicaltask.controllers.UserController;
import ru.webrise.technicaltask.dto.UpdateUserDTO;
import ru.webrise.technicaltask.dto.UserDTO;
import ru.webrise.technicaltask.models.User;
import ru.webrise.technicaltask.services.UserService;
import ru.webrise.technicaltask.util.exceptions.UserNotFoundException;
import ru.webrise.technicaltask.util.handlers.BindingResultErrorHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = TechnicalTaskApplication.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private BindingResultErrorHandler bindingResultErrorHandler;

    @Test
    void addUser_ShouldReturnOk() throws Exception {
        String userJson = """
            {
                "username": "testuser",
                "email": "test@example.com",
                "fullName": "Test User"
            }
            """;

        Mockito.when(userService.saveUser(Mockito.any(UserDTO.class)))
                .thenReturn(1L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.message").value("User was successfully added"));

        Mockito.verify(userService, Mockito.times(1)).saveUser(Mockito.any(UserDTO.class));
    }

    @Test
    void addUser_InvalidData_ShouldReturnBadRequest() throws Exception {
        String invalidUserJson = """
            {
                "username": "",
                "email": "invalid-email",
                "fullName": ""
            }
            """;

        Mockito.doAnswer(invocation -> {
            BindingResult result = invocation.getArgument(0);
            if (result.hasErrors()) {
                throw new MethodArgumentNotValidException(null, result);
            }
            return null;
        }).when(bindingResultErrorHandler).handleError(Mockito.any(BindingResult.class));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest());

        Mockito.verify(bindingResultErrorHandler, Mockito.times(1))
                .handleError(Mockito.any(BindingResult.class));
    }

    @Test
    void getUser_ShouldReturnUser() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");

        Mockito.when(userService.getUserInfo(1L))
                .thenReturn(mockUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUser_NotFound_ShouldReturnNotFound() throws Exception {
        Mockito.when(userService.getUserInfo(999L))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        String updateJson = """
            {
                "username": "updateduser",
                "email": "updated@example.com",
                "fullName": "Updated User"
            }
            """;

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User was successfully updated"));

        Mockito.verify(userService, Mockito.times(1))
                .updateUser(Mockito.eq(1L), Mockito.any(UpdateUserDTO.class));
    }

    @Test
    void deleteUser_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User was successfully deleted"));

        Mockito.verify(userService, Mockito.times(1)).deleteUser(1L);
    }
}