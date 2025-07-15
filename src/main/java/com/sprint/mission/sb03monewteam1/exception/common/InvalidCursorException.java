package com.sprint.mission.sb03monewteam1.exception.common;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class InvalidCursorException extends CommonException {

    public InvalidCursorException() {
        super(ErrorCode.INVALID_CURSOR_FORMAT);
    }

    public InvalidCursorException(String cursor) {
        super(ErrorCode.INVALID_CURSOR_FORMAT, Map.of("cursor", cursor));
    }

    public InvalidCursorException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidCursorException(ErrorCode errorCode, String cursor) {
        super(errorCode, Map.of("cursor", cursor));
    }
}
