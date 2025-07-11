package com.sprint.mission.sb03monewteam1.exception.user;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class EmailAlreadyExistsException extends UserException {

    public EmailAlreadyExistsException(String email) {
        super(ErrorCode.EMAIL_ALREADY_EXISTS, Map.of("email", email));
    }
}