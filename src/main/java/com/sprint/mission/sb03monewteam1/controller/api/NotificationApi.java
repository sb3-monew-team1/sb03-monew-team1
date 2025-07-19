package com.sprint.mission.sb03monewteam1.controller.api;

import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "알림 관리", description = "알림 관련 API")
public interface NotificationApi {

    @Operation(summary = "알림 목록 조회")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "알림 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = CursorPageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<CursorPageResponse<NotificationDto>> getNotifications(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Instant after,
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
        @RequestHeader("Monew-Request-User-ID") UUID userId
    );

    @Operation(summary = "알림 확인")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "알림 확인 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = NotificationDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "대상자 아님",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<NotificationDto> confirm(
        @Parameter(description = "알림 ID", required = true) @PathVariable UUID notificationId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") UUID userId
    );
}
