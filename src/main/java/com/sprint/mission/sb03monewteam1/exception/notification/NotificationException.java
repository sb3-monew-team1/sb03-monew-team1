package com.sprint.mission.sb03monewteam1.exception.notification;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class NotificationException extends CustomException {

    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationException(ErrorCode errorCode, Map<String, String> details) {
        super(errorCode, details);
    }

}
