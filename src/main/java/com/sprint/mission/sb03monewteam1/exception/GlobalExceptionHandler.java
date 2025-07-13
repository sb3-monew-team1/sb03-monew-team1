package com.sprint.mission.sb03monewteam1.exception;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getErrorCode().name(), e);

        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(ErrorResponse.of(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ErrorResponse.of(errorCode, e));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
        MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        log.warn("[Validation] 유효성 검사 실패 - {}", e.getMessage());

        // 전체 메시지: 각 필드별 메시지를 조합
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> Optional.ofNullable(error.getDefaultMessage())
                .orElse("유효성 검사 실패"))
            .collect(Collectors.joining(", "));

        // details: 필드명을 키로, 에러 메시지를 값으로 하는 맵
        Map<String, String> details = e.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> Optional.ofNullable(error.getDefaultMessage())
                    .orElse("유효성 검사 실패"),
                (existing, replacement) -> existing + ", " + replacement
            ));

        ErrorResponse response = ErrorResponse.of(
            "VALIDATION_ERROR",
            message,
            details,
            e.getClass().getSimpleName(),
            status
        );

        return ResponseEntity.status(status).body(response);

    }
}
