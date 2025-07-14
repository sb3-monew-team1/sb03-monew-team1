package com.sprint.mission.sb03monewteam1.exception.user;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class MissMonewIdHeaderException extends UserException {

    public MissMonewIdHeaderException(String message) {
        super(ErrorCode.MISS_REQUEST_HEADER, Map.of("message", message));
    }
}
