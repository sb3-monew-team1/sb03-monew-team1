package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ArticleService {

    ArticleViewDto createArticleView(UUID userId, UUID articleId);

    CursorPageResponse<ArticleDto> getArticles(
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

    void collectAndSaveNaverArticles(Interest interest, String keyword);

    void collectAndSaveHankyungArticles(Interest interest, String keyword);
}
