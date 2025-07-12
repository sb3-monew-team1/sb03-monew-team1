package com.sprint.mission.sb03monewteam1.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseArticleDto;

public interface ArticleService {

    ArticleViewDto createArticleView(UUID userId, UUID articleId);

    CursorPageResponseArticleDto getArticles(
            String keyword,
            List<String> sourceIn,
            List<String> interests,
            Instant publishDateFrom,
            Instant publishDateTo,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit);

    List<String> getSources();
}
