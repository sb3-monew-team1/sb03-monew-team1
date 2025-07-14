package com.sprint.mission.sb03monewteam1.exception.user;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class ForbiddenAccessException extends UserException {

    public ForbiddenAccessException(String message) {
        super(ErrorCode.FORBIDDEN_ACCESS, Map.of("message", message));
    }
}
