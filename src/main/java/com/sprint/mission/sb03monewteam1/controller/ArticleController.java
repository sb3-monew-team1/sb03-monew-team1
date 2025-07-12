package com.sprint.mission.sb03monewteam1.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.sprint.mission.sb03monewteam1.controller.api.ArticleApi;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseArticleDto;
import com.sprint.mission.sb03monewteam1.service.ArticleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ArticleController implements ArticleApi {

    private final ArticleService articleService;

    @Override
    public ResponseEntity<ArticleViewDto> createArticleView(UUID userId, UUID articleId) {
        log.info("기사 뷰 등록 요청 - userId: {}, articleId: {}", userId, articleId);
        ArticleViewDto result = articleService.createArticleView(userId, articleId);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<CursorPageResponseArticleDto> getArticles(
            String keyword,
            List<String> sourceIn,
            List<String> interests,
            Instant publishDateFrom,
            Instant publishDateTo,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit) {

        CursorPageResponseArticleDto result = articleService.getArticles(
                keyword, sourceIn, interests, publishDateFrom, publishDateTo,
                orderBy, direction, cursor, after, limit);

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<String>> getSources() {
        log.info("기사 출처 목록 조회 요청");
        List<String> sources = articleService.getSources();
        return ResponseEntity.ok(sources);
    }
}
