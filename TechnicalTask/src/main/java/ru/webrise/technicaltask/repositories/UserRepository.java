package ru.webrise.technicaltask.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.webrise.technicaltask.models.User;
import ru.webrise.technicaltask.util.exceptions.NonUniqueEmailException;
import ru.webrise.technicaltask.util.exceptions.NonUniqueUsernameException;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
        SELECT u FROM User u
            LEFT JOIN FETCH u.subscriptions s
        WHERE u.id = :userId AND s.active = true
    """)
    Optional<User> findByIdAndSubscriptionsActive(@Param("userId") long userId);

    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :userId")
    void deleteUser(@Param("userId") long userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    default void checkUsernameUnique(String username) {
        if (existsByUsername(username)) {
            throw new NonUniqueUsernameException("User with this username already exists");
        }
    }

    default void checkEmailUnique(String email) {
        if (existsByEmail(email)) {
            throw new NonUniqueEmailException("User with this email already exists");
        }
    }
}
