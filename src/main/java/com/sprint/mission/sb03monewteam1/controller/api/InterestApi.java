package com.sprint.mission.sb03monewteam1.controller.api;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.InterestUpdateRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "관심사 관리", description = "관심사 관련 API")
public interface InterestApi {

    @Operation(summary = "관심사 등록", description = "사용자가 관심사를 등록합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "관심사 등록 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InterestDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 존재하는 관심사",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "유사한 관심사 존재",
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
    ResponseEntity<InterestDto> createInterest(
        InterestRegisterRequest interestRegisterRequest);

    @Operation(summary = "관심사 목록 조회", description = "사용자가 관심사 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "관심사 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CursorPageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (잘못된 커서 형식, 정렬 기준 등)",
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
    ResponseEntity<CursorPageResponse<InterestDto>> getInterests(
        @RequestHeader("Monew-Request-User-ID") UUID userId,
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "") String cursor,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "subscriberCount") String orderBy,
        @RequestParam(defaultValue = "DESC") String direction);

    @Operation(summary = "관심사 구독", description = "사용자가 특정 관심사를 구독합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "구독 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubscriptionDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "관심사를 찾을 수 없음",
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
    ResponseEntity<SubscriptionDto> createSubscription(
        @PathVariable UUID interestId,
        @RequestHeader("Monew-Request-User-ID") UUID userId);

    @Operation(summary = "관심사 수정", description = "사용자가 관심사를 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "관심사 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InterestDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "관심사를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (입력값 검증 실패)",
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
    ResponseEntity<InterestDto> updateInterestKeywords(
        @PathVariable UUID interestId,
        @RequestBody InterestUpdateRequest request,
        @RequestHeader("Monew-Request-User-ID") UUID userId);

    @Operation(summary = "관심사 삭제", description = "사용자가 특정 관심사를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "관심사 삭제 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "관심사를 찾을 수 없음",
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
    ResponseEntity<Void> deleteInterest(
        @PathVariable UUID interestId);

    @Operation(summary = "관심사 구독 취소", description = "사용자가 특정 관심사에 대한 구독을 취소합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "구독 취소 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "관심사 또는 구독 정보가 존재하지 않음",
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
    ResponseEntity<Void> deleteSubscription(
        @PathVariable UUID interestId,
        @RequestHeader("Monew-Request-User-ID") UUID userId);
}
