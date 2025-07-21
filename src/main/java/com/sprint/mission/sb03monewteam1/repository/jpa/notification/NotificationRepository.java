package com.sprint.mission.sb03monewteam1.repository.jpa.notification;

import com.sprint.mission.sb03monewteam1.entity.Notification;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

    long countByUserIdAndIsCheckedFalse(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        UPDATE notifications
           SET is_checked = true,
               updated_at = CURRENT_TIMESTAMP
         WHERE user_id = :userId
           AND is_checked = false
    """, nativeQuery = true)
    int markAllAsCheckedByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.isChecked = true AND n.createdAt < :threshold")
    int deleteCheckedNotificationsBefore(@Param("threshold") Instant threshold);
}
