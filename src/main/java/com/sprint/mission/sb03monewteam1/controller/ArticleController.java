package com.sprint.mission.sb03monewteam1.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sprint.mission.sb03monewteam1.controller.api.ArticleApi;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.service.ArticleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController implements ArticleApi {

    private final ArticleService articleService;

    @Override
    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> createArticleView(
        @RequestHeader("Monew-Request-User-ID") UUID userId,
        @PathVariable UUID articleId) {
        log.info("기사 뷰 등록 요청 - userId: {}, articleId: {}", userId, articleId);
        ArticleViewDto result = articleService.createArticleView(userId, articleId);
        return ResponseEntity.ok(result);
    }

    @Override
    @GetMapping
    public ResponseEntity<CursorPageResponse<ArticleDto>> getArticles(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) List<String> sourceIn,
        @RequestParam(required = false) List<String> interests,
        @RequestParam(required = false) String publishDateFrom,
        @RequestParam(required = false) String publishDateTo,
        @RequestParam(required = false) String orderBy,
        @RequestParam(required = false) String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Instant after,
        @RequestParam(defaultValue = "10") int limit) {

        CursorPageResponse<ArticleDto> result = articleService.getArticles(
            keyword, sourceIn, interests, publishDateFrom, publishDateTo,
            orderBy, direction, cursor, after, limit);

        return ResponseEntity.ok(result);
    }

    @Override
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        log.info("기사 출처 목록 조회 요청");
        List<String> sources = articleService.getSources();
        return ResponseEntity.ok(sources);
    }

    @Override
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> delete(@PathVariable UUID articleId) {
        log.info("기사 삭제 요청 - articleId: {}", articleId);
        articleService.delete(articleId);
        return ResponseEntity.noContent().build();
    }
}
