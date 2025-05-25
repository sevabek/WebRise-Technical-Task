package ru.webrise.technicaltask.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.webrise.technicaltask.dto.SubscriptionStats;
import ru.webrise.technicaltask.dto.UserSubscriptionsDTO;
import ru.webrise.technicaltask.models.Subscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("""
        SELECT new ru.webrise.technicaltask.dto.UserSubscriptionsDTO(
            s.id,
            s.startDate,
            s.endDate,
            s.active,
            s.user.id,
            s.subscriptionProvider
        )
        FROM Subscription s
        WHERE s.active = true AND s.user.id = :userId
    """)
    List<UserSubscriptionsDTO> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT new ru.webrise.technicaltask.dto.SubscriptionStats(sp.name, COUNT(s.id))
        FROM Subscription s
        JOIN s.subscriptionProvider sp ON s.subscriptionProvider.id = sp.id
        WHERE s.active = true
        GROUP BY sp.name
        ORDER BY COUNT(s.id) DESC
    """)
    List<SubscriptionStats> findTopBySubscriptions(Pageable pageable);

    Optional<Subscription> findByIdAndUser_Id(Long subId, Long userId);

    boolean existsBySubscriptionProvider_IdAndUser_Id(Long subscriptionProviderId, Long userId);
}
