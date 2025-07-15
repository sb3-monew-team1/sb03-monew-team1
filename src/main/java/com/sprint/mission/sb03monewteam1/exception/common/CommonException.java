package com.sprint.mission.sb03monewteam1.exception.common;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class CommonException extends CustomException {

    public CommonException(ErrorCode errorCode, Map<String, String> details) {
        super(errorCode, details);
    }

    public CommonException(ErrorCode errorCode) {
        super(errorCode);
    }
}
