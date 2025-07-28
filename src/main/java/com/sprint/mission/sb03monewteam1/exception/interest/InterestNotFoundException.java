package com.sprint.mission.sb03monewteam1.exception.interest;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class InterestNotFoundException extends CustomException {

    public InterestNotFoundException(UUID interestId) {
        super(ErrorCode.INTEREST_NOT_FOUND, Map.of("interestID", String.valueOf(interestId)));
    }
}
