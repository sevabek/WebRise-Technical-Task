package ru.webrise.technicaltask.repositories;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.webrise.technicaltask.models.SubscriptionProvider;

import java.util.Optional;

@Repository
public interface SubscriptionProviderRepository extends JpaRepository<SubscriptionProvider, Long> {
    Optional<Object> findByName(String name);

    boolean existsByName(@Length(max = 50, message = "Service name must be under 50 characters") @NotBlank String name);
}
