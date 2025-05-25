package ru.webrise.technicaltask.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import ru.webrise.technicaltask.dto.SubscriptionDTO;
import ru.webrise.technicaltask.dto.UpdateUserDTO;
import ru.webrise.technicaltask.dto.UserDTO;
import ru.webrise.technicaltask.models.Subscription;
import ru.webrise.technicaltask.models.User;
import ru.webrise.technicaltask.repositories.UserRepository;
import ru.webrise.technicaltask.util.exceptions.NonUniqueEmailException;
import ru.webrise.technicaltask.util.exceptions.NonUniqueUsernameException;
import ru.webrise.technicaltask.util.exceptions.UserNotFoundException;
import ru.webrise.technicaltask.controllers.UserController;
import ru.webrise.technicaltask.controllers.SubscriptionController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с пользователями.
 * <p>
 * Обеспечивает бизнес-логику управления пользователями, включая регистрацию,
 * обновление данных, удаление и получение информации о пользователях.
 * <p>
 * Используется в контроллерах:
 * <ul>
 *     <li>{@link UserController} - для всех операций с пользователями</li>
 *     <li>{@link SubscriptionController} - при управлении подписками пользователей</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserServiceInterface {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SubscriptionProviderService subscriptionProviderService;

    /**
     * Создает нового пользователя. Можно создать только пользователя или уже сразу с подписками.
     * <p>
     * Используется в {@link UserController#addUser(UserDTO, BindingResult)}
     * при регистрации нового пользователя.
     * </p>
     *
     * @param userDTO DTO с данными пользователя
     * @throws NonUniqueUsernameException если имя пользователя уже занято
     * @throws NonUniqueEmailException    если email уже используется
     */
    @Override
    @Transactional
    public long saveUser(UserDTO userDTO) {
        log.info("Attempting to create new user with username: {}", userDTO.getUsername());

        try {
            userRepository.checkUsernameUnique(userDTO.getUsername());
            userRepository.checkEmailUnique(userDTO.getEmail());
        } catch (NonUniqueUsernameException e) {
            log.error("Failed to save user: user with username '{}' already exists", userDTO.getUsername());
            throw e;
        } catch (NonUniqueEmailException e) {
            log.error("Failed to save user: user with email '{}' already exists", userDTO.getEmail());
            throw e;
        }

        List<SubscriptionDTO> subscriptionDTOS = userDTO.getSubscriptions();
        userDTO.setSubscriptions(null);
        User user = convertToUser(userDTO);
        User savedUser = userRepository.save(user);

        if (subscriptionDTOS != null && !subscriptionDTOS.isEmpty()) {
            List<Subscription> subscriptions = subscriptionDTOS.stream()
                    .map(subDto -> {
                        Subscription sub = convertToSubscription(subDto);
                        sub.setUser(user);
                        sub.setSubscriptionProvider(
                                subscriptionProviderService.getSubscriptionProviderInfo(
                                        subDto.getSubscriptionProvider().getId()
                                )
                        );
                        return sub;
                    }).collect(Collectors.toList());

            user.setSubscriptions(subscriptions);
            userRepository.save(user);
            log.info("Successfully created user with ID: {} and {} subscriptions",
                    savedUser.getId(), subscriptions.size());
        } else
            log.info("Successfully created user with ID: {} without subscriptions", savedUser.getId());

        return savedUser.getId();
    }

    /**
     * Обновляет данные пользователя.
     * <p>
     * Используется в {@link UserController#updateUser(long, UpdateUserDTO, BindingResult)}
     * при изменении данных пользователя.
     * </p>
     *
     * @param userId  ID пользователя
     * @param userDTO DTO с обновляемыми данными
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    @Transactional
    public void updateUser(long userId, UpdateUserDTO userDTO) {
        log.info("Attempting to update user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Failed to update user: user with ID {} not found for update", userId);
                    return new UserNotFoundException("User with that id was not found");
                });

        try {
            userRepository.checkUsernameUnique(userDTO.getUsername());
            userRepository.checkEmailUnique(userDTO.getEmail());
        } catch (NonUniqueUsernameException e) {
            log.error("Failed to update user: user with username '{}' already exists", userDTO.getUsername());
            throw e;
        } catch (NonUniqueEmailException e) {
            log.error("Failed to update user: user with email '{}' already exists", userDTO.getEmail());
            throw e;
        }

        convertToUser(userDTO, user);
        userRepository.save(user);
        log.info("Successfully updated user with ID: {}", userId);
    }

    /**
     * Удаляет пользователя.
     * <p>
     * Используется в {@link UserController#deleteUser(long)}
     * при удалении учетной записи пользователя.
     * </p>
     *
     * @param userId ID удаляемого пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    @Transactional
    public void deleteUser(long userId) {
        log.info("Attempting to delete user with ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.error("Failed to delete user: user with ID {} not found for deletion", userId);
            throw new UserNotFoundException("User with that id was not found");
        }

        userRepository.deleteUser(userId);
        log.info("Successfully deleted user with ID: {}", userId);
    }

    /**
     * Получает информацию о пользователе.
     * <p>
     * Используется в:
     * <ul>
     *     <li>{@link UserController#getUser(long)} - для просмотра профиля</li>
     *     <li>{@link SubscriptionController} - при проверке существования пользователя</li>
     * </ul>
     * </p>
     *
     * @param id ID пользователя
     * @return Сущность пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public User getUserInfo(Long id) {
        log.debug("Fetching user info for ID: {}", id);
        return userRepository.findByIdAndSubscriptionsActive(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new UserNotFoundException("User with that id was not found");
                });
    }

    /**
     * Конвертирует UpdateUserDTO в существующую сущность User.
     *
     * @param userDTO DTO с обновляемыми данными
     * @param user    Сущность для обновления
     */
    private void convertToUser(UpdateUserDTO userDTO, User user) {
        log.trace("Updating User entity from UpdateUserDTO");
        modelMapper.map(userDTO, user);
    }

    /**
     * Конвертирует UserDTO в новую сущность User.
     *
     * @param userDTO DTO с данными нового пользователя
     * @return Новая сущность User
     */
    private User convertToUser(UserDTO userDTO) {
        log.trace("Converting UserDTO to User entity");
        return modelMapper.map(userDTO, User.class);
    }

    /**
     * Конвертирует SubscriptionDTO в новую сущность Subscription.
     *
     * @param subDto DTO с данными новой подписки
     * @return Новая сущность Subscription
     */
    private Subscription convertToSubscription(SubscriptionDTO subDto) {
        log.trace("Converting SubscriptionDTO to Subscription entity");
        return modelMapper.map(subDto, Subscription.class);
    }
}
