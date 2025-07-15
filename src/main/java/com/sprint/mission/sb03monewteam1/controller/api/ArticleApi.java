package com.sprint.mission.sb03monewteam1.controller.api;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "기사 관리", description = "기사 조회 및 뷰 관리 API")
@RequestMapping("/api/articles")
public interface ArticleApi {

    @Operation(summary = "기사 뷰 등록", description = "사용자가 기사를 조회했음을 기록합니다.")
    @PostMapping("/{articleId}/article-views")
    ResponseEntity<ArticleViewDto> createArticleView(
        @RequestHeader("Monew-Request-User-ID") UUID userId,
        @PathVariable UUID articleId);

    @Operation(summary = "기사 목록 조회", description = "조건에 맞는 기사 목록을 조회합니다.")
    @GetMapping
    ResponseEntity<CursorPageResponse<ArticleDto>> getArticles(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) List<String> sourceIn,
        @RequestParam(required = false) List<String> interests,
        @RequestParam(required = false) Instant publishDateFrom,
        @RequestParam(required = false) Instant publishDateTo,
        @RequestParam(required = false) String orderBy,
        @RequestParam(required = false) String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Instant after,
        @RequestParam(defaultValue = "10") int limit);

    @Operation(summary = "출처 목록 조회", description = "기사의 출처 목록을 조회합니다.")
    @GetMapping("/sources")
    ResponseEntity<List<String>> getSources();
}
