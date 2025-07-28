package com.sprint.mission.sb03monewteam1.controller.api;

import com.sprint.mission.sb03monewteam1.dto.UserActivityDto;
import com.sprint.mission.sb03monewteam1.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "사용자 활동 내역 관리", description = "사용자의 기사 조회, 댓글, 좋아요, 구독 활동 조회 API")
public interface UserActivityApi {

    @Operation(summary = "사용자 활동 조회", description = "특정 사용자의 활동 내역(조회, 댓글, 좋아요, 구독 등)을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "사용자 활동 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserActivityDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<UserActivityDto> getUserActivity(@PathVariable UUID userId);
}