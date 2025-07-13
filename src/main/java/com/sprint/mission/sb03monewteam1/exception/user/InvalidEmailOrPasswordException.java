package com.sprint.mission.sb03monewteam1.exception.user;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class InvalidEmailOrPasswordException extends UserException {

    public InvalidEmailOrPasswordException(String credential) {
        super(ErrorCode.INVALID_USER_CREDENTIALS, Map.of("email", credential));
    }
}
