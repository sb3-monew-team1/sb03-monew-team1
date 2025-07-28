package com.sprint.mission.sb03monewteam1.exception.notification;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class NotificationCleanupException extends NotificationException {

    public NotificationCleanupException(String message) {
        super(ErrorCode.NOTIFICATION_CLEANUP_FAILED, Map.of("message", message));
    }
}
