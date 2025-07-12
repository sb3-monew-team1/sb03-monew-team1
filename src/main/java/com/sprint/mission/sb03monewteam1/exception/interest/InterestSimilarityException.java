package com.sprint.mission.sb03monewteam1.exception.interest;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class InterestSimilarityException extends InterestException {

    public InterestSimilarityException(String name) {
        super(ErrorCode.INTEREST_SIMILARITY_ERROR, Map.of("name", name));
    }
}