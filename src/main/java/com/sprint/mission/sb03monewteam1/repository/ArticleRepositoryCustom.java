package com.sprint.mission.sb03monewteam1.repository;

import java.time.Instant;
import java.util.List;

import com.sprint.mission.sb03monewteam1.entity.Article;

public interface ArticleRepositoryCustom {

    // 검색 조건 기반 조회
    List<Article> findArticlesWithConditions(
            String searchKeyword,
            String source,
            Instant startDate,
            Instant endDate);

    // 커서 페이지네이션 - 날짜 기준 정렬
    List<Article> findArticlesWithCursorByDate(
            String searchKeyword,
            String source,
            Instant startDate,
            Instant endDate,
            Instant cursor,
            int limit);

    // 커서 페이지네이션 - 조회수 기준 정렬
    List<Article> findArticlesWithCursorByViewCount(
            String searchKeyword,
            String source,
            Instant startDate,
            Instant endDate,
            Long cursor,
            int limit);

    // 출처 목록 조회
    List<String> findDistinctSources();
}