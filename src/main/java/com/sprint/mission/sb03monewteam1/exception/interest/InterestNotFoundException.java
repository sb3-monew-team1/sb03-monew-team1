package com.sprint.mission.sb03monewteam1.exception.interest;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class InterestNotFoundException extends CustomException {

    public InterestNotFoundException(ErrorCode errorCode, Map<String, String> details) {
        super(ErrorCode.INTEREST_NOT_FOUND, details);
    }

    public InterestNotFoundException(ErrorCode errorCode) {
        super(ErrorCode.INTEREST_NOT_FOUND);
    }
}
