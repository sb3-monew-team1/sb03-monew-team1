package com.sprint.mission.sb03monewteam1.exception.notification;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NotificationAccessDeniedException extends NotificationException {

    public NotificationAccessDeniedException(UUID userId) {
        super(ErrorCode.FORBIDDEN_ACCESS, Map.of("userId", String.valueOf(userId)));
    }
}
