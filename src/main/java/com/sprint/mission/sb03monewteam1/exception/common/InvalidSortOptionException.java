package com.sprint.mission.sb03monewteam1.exception.common;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class InvalidSortOptionException extends CommonException {

    public InvalidSortOptionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidSortOptionException(ErrorCode errorCode, String sortBy) {
        super(errorCode, Map.of("sortBy", sortBy));
    }

    public InvalidSortOptionException(String sortBy) {
        super(ErrorCode.INVALID_SORT_FIELD, Map.of("sortBy", sortBy));
    }
}