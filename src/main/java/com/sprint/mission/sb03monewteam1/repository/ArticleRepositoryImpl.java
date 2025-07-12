package com.sprint.mission.sb03monewteam1.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.QArticle;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QArticle article = QArticle.article;

    @Override
    public List<Article> findArticlesWithConditions(
        String searchKeyword,
        String source,
        Instant startDate,
        Instant endDate) {

        log.debug("조건부 기사 조회 - searchKeyword: {}, source: {}, startDate: {}, endDate: {}",
            searchKeyword, source, startDate, endDate);

        BooleanBuilder builder = createBaseCondition();
        addSearchConditions(builder, searchKeyword, source, startDate, endDate);

        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(article.publishDate.desc())
            .fetch();
    }

    @Override
    public List<Article> findArticlesWithCursorByDate(
        String searchKeyword,
        String source,
        Instant startDate,
        Instant endDate,
        Instant cursor,
        int limit) {

        log.debug("날짜 기준 커서 페이지네이션 기사 조회 - cursor: {}, limit: {}", cursor, limit);

        BooleanBuilder builder = createBaseCondition();
        addSearchConditions(builder, searchKeyword, source, startDate, endDate);
        addCursorCondition(builder, cursor, null, "date");

        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(article.publishDate.desc())
            .limit(limit)
            .fetch();
    }

    @Override
    public List<Article> findArticlesWithCursorByViewCount(
        String searchKeyword,
        String source,
        Instant startDate,
        Instant endDate,
        Long cursor,
        int limit) {

        log.debug("조회수 기준 커서 페이지네이션 기사 조회 - cursor: {}, limit: {}", cursor, limit);

        BooleanBuilder builder = createBaseCondition();
        addSearchConditions(builder, searchKeyword, source, startDate, endDate);
        addCursorCondition(builder, null, cursor, "viewCount");

        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(article.viewCount.desc(), article.publishDate.desc()) // 조회수 같을 때 날짜로 정렬
            .limit(limit)
            .fetch();
    }

    @Override
    public List<String> findDistinctSources() {
        log.debug("기사 출처 목록 조회");

        return queryFactory
            .select(article.source)
            .distinct()
            .from(article)
            .where(article.isDeleted.isFalse())
            .orderBy(article.source.asc())
            .fetch();
    }

    /**
     * 기본 조건 (논리 삭제 제외)
     */
    private BooleanBuilder createBaseCondition() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(article.isDeleted.isFalse());
        return builder;
    }

    /**
     * 검색 조건 추가
     */
    private void addSearchConditions(BooleanBuilder builder, String searchKeyword, String source,
        Instant startDate, Instant endDate) {

        // 키워드 검색 (제목, 요약)
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            builder.and(
                article.title.containsIgnoreCase(searchKeyword)
                    .or(article.summary.containsIgnoreCase(searchKeyword)));
        }

        // 출처 필터
        if (source != null && !source.trim().isEmpty()) {
            builder.and(article.source.eq(source));
        }

        // 시작 날짜 필터
        if (startDate != null) {
            builder.and(article.publishDate.goe(startDate));
        }

        // 종료 날짜 필터
        if (endDate != null) {
            builder.and(article.publishDate.loe(endDate));
        }
    }

    /**
     * 커서 조건 추가
     */
    private void addCursorCondition(BooleanBuilder builder, Instant dateCursor,
        Long viewCountCursor, String sortBy) {
        if ("date".equals(sortBy) && dateCursor != null) {
            builder.and(article.publishDate.lt(dateCursor));
        } else if ("viewCount".equals(sortBy) && viewCountCursor != null) {
            builder.and(article.viewCount.lt(viewCountCursor));
        }
    }
}
