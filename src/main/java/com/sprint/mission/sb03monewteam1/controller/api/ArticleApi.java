package com.sprint.mission.sb03monewteam1.controller.api;


import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "기사 관리", description = "기사 조회 및 뷰 관리 API")
public interface ArticleApi {

    @Operation(summary = "기사 뷰 등록", description = "사용자가 기사를 조회했음을 기록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(schema = @Schema(implementation = ArticleViewDto.class))),
        @ApiResponse(responseCode = "404", description = "기사 정보 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ArticleViewDto> createArticleView(
        @RequestHeader("Monew-Request-User-ID") UUID userId,
        @PathVariable UUID articleId);

    @Operation(summary = "기사 목록 조회", description = "조건에 맞는 기사 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CursorPageResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<CursorPageResponse<ArticleDto>> getArticles(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) List<String> sourceIn,
        @RequestParam(required = false) List<String> interests,
        @RequestParam(required = false) String publishDateFrom,
        @RequestParam(required = false) String publishDateTo,
        @RequestParam(required = false) String orderBy,
        @RequestParam(required = false) String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Instant after,
        @RequestParam(defaultValue = "10") int limit,
        @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID userId);

    @Operation(summary = "출처 목록 조회", description = "기사의 출처 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<String>> getSources();

    @Operation(summary = "기사 논리 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "논리 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "기사 정보 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> delete(@PathVariable UUID articleId);

    @Operation(summary = "기사 물리 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "기사 정보 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deleteHard(@PathVariable UUID articleId);
}
