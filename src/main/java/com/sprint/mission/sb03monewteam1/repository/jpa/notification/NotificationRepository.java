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
    @Transactional(readOnly = false)
    @Query("""
        UPDATE Notification n
          SET n.isChecked = true,
                n.updatedAt = CURRENT_TIMESTAMP
        WHERE n.user.id = :userId
          AND n.isChecked = false
      """)
    int markAllAsCheckedByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.isChecked = true AND n.createdAt < :threshold")
    int deleteCheckedNotificationsBefore(@Param("threshold") Instant threshold);
}
