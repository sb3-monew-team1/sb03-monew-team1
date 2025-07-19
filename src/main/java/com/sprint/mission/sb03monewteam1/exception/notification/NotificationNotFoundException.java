package com.sprint.mission.sb03monewteam1.exception.notification;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

  public NotificationNotFoundException(UUID notificationId) {
    super(ErrorCode.NOTIFICATION_NOT_FOUND, Map.of("notificationId", String.valueOf(notificationId)));
  }
}
