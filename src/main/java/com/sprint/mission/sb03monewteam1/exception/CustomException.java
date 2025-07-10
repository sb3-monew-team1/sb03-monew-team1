package com.sprint.mission.sb03monewteam1.exception;

import java.util.Collections;
import java.util.Map;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, String> details;

    public CustomException(ErrorCode errorCode, Map<String, String> details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = details == null ? Collections.emptyMap() : details;
    }

    public CustomException(ErrorCode errorCode, Map<String, String> details) {
        this(errorCode, details, null);
    }

    public CustomException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, Collections.emptyMap(), cause);
    }

    public CustomException(ErrorCode errorCode) {
        this(errorCode, Collections.emptyMap(), null);
    }
}
