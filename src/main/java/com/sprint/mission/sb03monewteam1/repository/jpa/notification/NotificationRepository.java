package com.sprint.mission.sb03monewteam1.repository.jpa.notification;

import com.sprint.mission.sb03monewteam1.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

    long countByUserIdAndIsCheckedFalse(UUID userId);

    List<Notification> findByUserIdAndIsCheckedFalse(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Notification n
          SET n.isChecked = true
        WHERE n.user.id = :userId
          AND n.isChecked = false
      """)
    int markAllAsCheckedByUserId(@Param("userId") UUID userId);
}
