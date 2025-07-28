package com.sprint.mission.sb03monewteam1.exception.interest;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class InterestException extends CustomException {

    public InterestException(ErrorCode errorCode, Map<String, String> details) {
        super(errorCode, details);
    }
}
