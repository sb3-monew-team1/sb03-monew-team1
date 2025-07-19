package com.sprint.mission.sb03monewteam1.repository.jpa.notification;

import com.sprint.mission.sb03monewteam1.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryCustom {

    List<Notification> findUncheckedNotificationsWithCursor(
        UUID userId,
        String cursor,
        Instant nextAfter,
        int limit
    );
}
