package com.sprint.mission.sb03monewteam1.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 예외
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    REQUEST_BODY_NOT_READABLE(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다. 형식을 확인해주세요."),

    // 사용자 관련 예외
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_USER_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),

    // 관심사 관련 예외
    INTEREST_SIMILARITY_ERROR(HttpStatus.CONFLICT, "유사한 관심사 이름이 존재합니다."),
    INTEREST_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 관심사입니다."),
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "관심사를 찾을 수 없습니다."),

    // 기사 관련 예외
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "기사를 찾을 수 없습니다."),
    DUPLICATE_ARTICLE_VIEW(HttpStatus.CONFLICT, "이미 조회한 기사입니다."),
    ARTICLE_COLLECTION_NETWORK_ERROR(HttpStatus.BAD_GATEWAY, "기사 수집 중 네트워크 오류가 발생했습니다."),
    ARTICLE_COLLECTION_PARSING_ERROR(HttpStatus.BAD_REQUEST, "기사 파싱 중 오류가 발생했습니다."),
    ARTICLE_COLLECTION_VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "수집 데이터 검증에 실패했습니다."),
    ARTICLE_COLLECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "기사 수집에 실패했습니다."),

    // 댓글 관련 예외
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    COMMENT_ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 누른 댓글입니다."),

    // 커서 기반 페이지네이션 관련 예외
    INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 커서 형식입니다."),
    INVALID_CURSOR_ID(HttpStatus.BAD_REQUEST,"잘못된 ID 커서 형식입니다."),
    INVALID_CURSOR_DATE(HttpStatus.BAD_REQUEST,"잘못된 날짜 커서 형식입니다."),
    INVALID_CURSOR_COUNT(HttpStatus.BAD_REQUEST,"잘못된 숫자 커서 형식입니다."),

    INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST,"지원하지 않는 정렬 필드입니다."),
    INVALID_SORT_DIRECTION(HttpStatus.BAD_REQUEST,"지원하지 않는 정렬 방향입니다."),

    // S3 관련 예외
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드에 실패했습니다."),
    S3_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 다운로드에 실패했습니다."),

    // 유효성 검증 관련 예외
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, ""),

    // Monew-Request-User-ID 헤더 관련 예외
    MISS_REQUEST_HEADER(HttpStatus.UNAUTHORIZED, "Monew-Request-User-ID 헤더를 찾을 수 없습니다");

    private final HttpStatus httpStatus;
    private final String message;
}
