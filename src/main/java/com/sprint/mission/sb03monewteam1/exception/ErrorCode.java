package com.sprint.mission.sb03monewteam1.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 예외
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,"지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    REQUEST_BODY_NOT_READABLE(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다. 형식을 확인해주세요.");

    private final HttpStatus httpStatus;
    private final String message;
}
