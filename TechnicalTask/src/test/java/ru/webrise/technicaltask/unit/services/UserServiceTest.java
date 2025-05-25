package ru.webrise.technicaltask.unit.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.webrise.technicaltask.dto.SubscriptionDTO;
import ru.webrise.technicaltask.dto.UpdateUserDTO;
import ru.webrise.technicaltask.dto.UserDTO;
import ru.webrise.technicaltask.models.Subscription;
import ru.webrise.technicaltask.models.SubscriptionProvider;
import ru.webrise.technicaltask.models.User;
import ru.webrise.technicaltask.repositories.UserRepository;
import ru.webrise.technicaltask.services.SubscriptionProviderService;
import ru.webrise.technicaltask.services.UserService;
import ru.webrise.technicaltask.util.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SubscriptionProviderService subscriptionProviderService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Создание пользователя без подписок - успех")
    void saveUser_WithoutSubscriptions_ShouldReturnUserId() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setSubscriptions(null);

        User user = new User();
        user.setId(1L);
        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        long userId = userService.saveUser(userDTO);

        assertEquals(1L, userId);
        verify(userRepository).save(user);
        verify(userRepository, never()).checkUsernameUnique(null);
        verify(userRepository, never()).checkEmailUnique(null);
    }

    @Test
    @DisplayName("Создание пользователя с подписками - успех")
    void saveUser_WithSubscriptions_ShouldSaveWithSubscriptions() {
        SubscriptionDTO subDto = new SubscriptionDTO();
        subDto.setSubscriptionProvider(new SubscriptionProvider(1L, null, null));

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setSubscriptions(List.of(subDto));

        User user = new User();
        user.setId(1L);

        SubscriptionProvider provider = new SubscriptionProvider();
        provider.setId(1L);

        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(subscriptionProviderService.getSubscriptionProviderInfo(1L)).thenReturn(provider);
        when(modelMapper.map(subDto, Subscription.class)).thenReturn(new Subscription());

        userService.saveUser(userDTO);

        verify(userRepository, times(2)).save(user);
        verify(subscriptionProviderService).getSubscriptionProviderInfo(1L);
    }

    @Test
    @DisplayName("Обновление пользователя - успех")
    void updateUser_ShouldUpdateSuccessfully() {
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setUsername("newusername");
        updateDTO.setEmail("new@example.com");

        User existingUser = new User();
        existingUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.updateUser(1L, updateDTO);

        verify(modelMapper).map(updateDTO, existingUser);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Удаление существующего пользователя - успех")
    void deleteUser_ExistingUser_ShouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteUser(1L);
    }

    @Test
    @DisplayName("Получение информации о пользователе - успех")
    void getUserInfo_ShouldReturnUser() {
        User expectedUser = new User();
        expectedUser.setId(1L);

        when(userRepository.findByIdAndSubscriptionsActive(1L)).thenReturn(Optional.of(expectedUser));

        User result = userService.getUserInfo(1L);

        assertEquals(expectedUser, result);
    }

    @Test
    @DisplayName("Получение информации о несуществующем пользователе - исключение")
    void getUserInfo_NonExistingUser_ShouldThrowException() {
        when(userRepository.findByIdAndSubscriptionsActive(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserInfo(1L));
    }
}
