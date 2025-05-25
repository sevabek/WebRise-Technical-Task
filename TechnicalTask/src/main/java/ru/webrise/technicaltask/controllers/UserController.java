package ru.webrise.technicaltask.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.webrise.technicaltask.dto.UpdateUserDTO;
import ru.webrise.technicaltask.dto.UserDTO;
import ru.webrise.technicaltask.models.User;
import ru.webrise.technicaltask.services.UserService;
import ru.webrise.technicaltask.util.handlers.BindingResultErrorHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для управления пользователями.
 * Предоставляет CRUD-эндпоинты для работы с пользователями.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BindingResultErrorHandler bindingResultErrorHandler;

    /**
     * Создает нового пользователя.
     *
     * @param userDTO DTO с данными нового пользователя
     * @param bindingResult Результат валидации входящих данных
     * @return Сообщение об успешном создании пользователя
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> addUser(
            @RequestBody @Valid UserDTO userDTO,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors())
            bindingResultErrorHandler.handleError(bindingResult);

        long id = userService.saveUser(userDTO);
        Map<String, String> response = new HashMap<>();
        response.put("id", String.valueOf(id));
        response.put("message", "User was successfully added");
        return ResponseEntity.ok(response);
    }

    /**
     * Получает информацию о пользователе по его идентификатору.
     *
     * @param userId Идентификатор пользователя
     * @return Данные пользователя в формате JSON
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable(name = "userId") long userId) {
        User user = userService.getUserInfo(userId);
        return ResponseEntity.ok().body(user);
    }

    /**
     * Обновляет данные существующего пользователя.
     *
     * @param userId Идентификатор обновляемого пользователя
     * @param updateUserDTO DTO с обновляемыми данными пользователя
     * @return Сообщение об успешном обновлении
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<String> updateUser(
            @PathVariable(name = "userId") long userId,
            @RequestBody @Valid UpdateUserDTO updateUserDTO,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors())
            bindingResultErrorHandler.handleError(bindingResult);

        userService.updateUser(userId, updateUserDTO);
        return ResponseEntity.ok("User was successfully updated");
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId Идентификатор удаляемого пользователя
     * @return Сообщение об успешном удалении
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "userId") long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User was successfully deleted");
    }
}
