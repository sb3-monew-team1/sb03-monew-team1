package com.sprint.mission.sb03monewteam1.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sprint.mission.sb03monewteam1.entity.Article;

public interface ArticleRepositoryCustom {

    List<Article> findArticlesWithCursorByDate(
            String keyword,
            List<String> sourceIn,
            Instant publishDateFrom,
            Instant publishDateTo,
            Instant cursor,
            int limit,
            boolean isAscending);

    List<Article> findArticlesWithCursorByViewCount(
            String keyword,
            List<String> sourceIn,
            Instant publishDateFrom,
            Instant publishDateTo,
            Long cursorViewCount,
            Instant cursorPublishDate,
            int limit,
            boolean isAscending);

    List<Article> findArticlesWithCursorByCommentCount(
            String keyword,
            List<String> sourceIn,
            Instant publishDateFrom,
            Instant publishDateTo,
            Long cursorCommentCount,
            Instant cursorPublishDate,
            int limit,
            boolean isAscending);

    List<String> findDistinctSources();

    long incrementViewCount(UUID articleId);
}