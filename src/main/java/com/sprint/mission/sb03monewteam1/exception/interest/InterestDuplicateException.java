package com.sprint.mission.sb03monewteam1.exception.interest;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class InterestDuplicateException extends InterestException {

    public InterestDuplicateException(String name) {
        super(ErrorCode.INTEREST_DUPLICATE, Map.of("name", name));
    }
}
