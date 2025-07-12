package com.sprint.mission.sb03monewteam1.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseArticleDto;

public interface ArticleService {
    ArticleViewDto createArticleView(UUID userId, UUID articleId);

    CursorPageResponseArticleDto getArticles(
            String searchKeyword,
            String source,
            List<String> interests,
            Instant startDate,
            Instant endDate,
            String sortBy,
            String cursor,
            int limit);

    List<String> getSources();
}
