package com.sprint.mission.sb03monewteam1.exception.user;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class UserException extends CustomException {

    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(ErrorCode errorCode, Map<String, String> details) {
        super(errorCode, details);
    }
}
