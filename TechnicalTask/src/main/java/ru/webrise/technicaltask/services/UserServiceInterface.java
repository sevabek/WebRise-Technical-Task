package ru.webrise.technicaltask.services;

import ru.webrise.technicaltask.dto.UpdateUserDTO;
import ru.webrise.technicaltask.dto.UserDTO;
import ru.webrise.technicaltask.models.User;

public interface UserServiceInterface {

    long saveUser(UserDTO userDTO);

    void updateUser(long userId, UpdateUserDTO userDTO);

    void deleteUser(long userId);

    User getUserInfo(Long id);
}
