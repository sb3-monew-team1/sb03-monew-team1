package com.sprint.mission.sb03monewteam1.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        Map<String, String> details,
        String exceptionType,
        int status
) {

    public static ErrorResponse of(String code, String message, Map<String, String> details,
        String exceptionType, HttpStatus status) {

        return new ErrorResponse(
            Instant.now(),
            code,
            message,
            details,
            exceptionType,
            status.value()
        );
    }

    public static ErrorResponse of(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        return new ErrorResponse(
                Instant.now(),
                errorCode.name(),
                errorCode.getMessage(),
                e.getDetails(),
                e.getClass().getSimpleName(),
                errorCode.getHttpStatus().value()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, Exception e) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.name(),
                errorCode.getMessage(),
                Collections.emptyMap(),
                e.getClass().getSimpleName(),
                errorCode.getHttpStatus().value()
        );
    }
}
