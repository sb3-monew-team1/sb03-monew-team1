package com.sprint.mission.sb03monewteam1.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sprint.mission.sb03monewteam1.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    // 기본 조회 (논리 삭제 제외)
    Optional<Article> findByIdAndIsDeletedFalse(UUID id);

    // 검색 조건 기반 조회 (제목, 요약 부분일치) - RED 단계에서는 메서드명만 정의
    List<Article> findArticlesWithConditions(
            @Param("searchKeyword") String searchKeyword,
            @Param("source") String source,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // 커서 페이지네이션 - 날짜 기준 정렬
    List<Article> findArticlesWithCursorByDate(
            @Param("searchKeyword") String searchKeyword,
            @Param("source") String source,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("cursor") Instant cursor,
            @Param("limit") int limit);

    // 커서 페이지네이션 - 조회수 기준 정렬
    List<Article> findArticlesWithCursorByViewCount(
            @Param("searchKeyword") String searchKeyword,
            @Param("source") String source,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("cursor") Long cursor,
            @Param("limit") int limit);

    // 출처 목록 조회
    List<String> findDistinctSources();
}
