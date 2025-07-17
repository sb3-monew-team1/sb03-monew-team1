package com.sprint.mission.sb03monewteam1.repository;

import com.sprint.mission.sb03monewteam1.entity.Subscription;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.interest WHERE s.user.id = :userId")
    List<Subscription> findAllByUserId(@Param("userId") UUID userId);

    @Transactional
    void deleteByUserId(UUID userId);
}
