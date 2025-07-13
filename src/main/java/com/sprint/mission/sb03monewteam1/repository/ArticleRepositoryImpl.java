package com.sprint.mission.sb03monewteam1.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.QArticle;
import com.sprint.mission.sb03monewteam1.mapper.ArticleMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QArticle article = QArticle.article;
    private final ArticleMapper articleMapper;

    @Override
    public List<Article> findArticlesWithCursorByDate(
        String keyword,
        List<String> sourceIn,
        Instant publishDateFrom,
        Instant publishDateTo,
        Instant cursor,
        int limit,
        boolean isAscending) {

        BooleanBuilder builder = createBaseCondition();
        addSearchConditions(builder, keyword, sourceIn, publishDateFrom, publishDateTo);
        addDateCursorCondition(builder, cursor, isAscending);

        OrderSpecifier<?> orderBy = isAscending
            ? article.publishDate.asc()
            : article.publishDate.desc();

        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(orderBy)
            .limit(limit)
            .fetch();
    }

    @Override
    public List<Article> findArticlesWithCursorByViewCount(
        String keyword,
        List<String> sourceIn,
        Instant publishDateFrom,
        Instant publishDateTo,
        Long cursorViewCount,
        Instant cursorPublishDate,
        int limit,
        boolean isAscending) {

        BooleanBuilder builder = createBaseCondition();
        addSearchConditions(builder, keyword, sourceIn, publishDateFrom, publishDateTo);
        addViewCountCursorCondition(builder, cursorViewCount, cursorPublishDate, isAscending);

        OrderSpecifier<?>[] orderBy = isAscending
            ? new OrderSpecifier[]{article.viewCount.asc(), article.publishDate.asc()}
            : new OrderSpecifier[]{article.viewCount.desc(), article.publishDate.desc()};

        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(orderBy)
            .limit(limit)
            .fetch();
    }

    @Override
    public List<Article> findArticlesWithCursorByCommentCount(
        String keyword,
        List<String> sourceIn,
        Instant publishDateFrom,
        Instant publishDateTo,
        Long cursorCommentCount,
        Instant cursorPublishDate,
        int limit,
        boolean isAscending) {

        BooleanBuilder builder = createBaseCondition();
        addSearchConditions(builder, keyword, sourceIn, publishDateFrom, publishDateTo);
        addCommentCountCursorCondition(builder, cursorCommentCount, cursorPublishDate, isAscending);

        OrderSpecifier<?>[] orderBy = isAscending
            ? new OrderSpecifier[]{article.commentCount.asc(), article.publishDate.asc()}
            : new OrderSpecifier[]{article.commentCount.desc(), article.publishDate.desc()};

        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(orderBy)
            .limit(limit)
            .fetch();
    }

    @Override
    public List<String> findDistinctSources() {
        return queryFactory
            .select(article.source)
            .distinct()
            .from(article)
            .where(article.isDeleted.eq(false))
            .orderBy(article.source.asc())
            .fetch();
    }

    @Override
    @Transactional
    public long incrementViewCount(UUID articleId) {
        return queryFactory
            .update(article)
            .set(article.viewCount, article.viewCount.add(1))
            .where(article.id.eq(articleId)
                .and(article.isDeleted.eq(false)))
            .execute();
    }

    @Override
    public List<ArticleDto> findAllCreatedYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Instant start = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = yesterday.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Article> articles = queryFactory
            .selectFrom(article)
            .where(article.createdAt.between(start, end))
            .fetch();

        return articles.stream()
            .map(articleMapper::toDto)
            .toList();
    }

    private BooleanBuilder createBaseCondition() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(article.isDeleted.isFalse());
        return builder;
    }

    private void addSearchConditions(BooleanBuilder builder, String keyword,
        List<String> sourceIn, Instant publishDateFrom, Instant publishDateTo) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(
                article.title.containsIgnoreCase(keyword)
                    .or(article.summary.containsIgnoreCase(keyword)));
        }

        if (sourceIn != null && !sourceIn.isEmpty()) {
            builder.and(article.source.in(sourceIn));
        }

        if (publishDateFrom != null) {
            builder.and(article.publishDate.goe(publishDateFrom));
        }

        if (publishDateTo != null) {
            builder.and(article.publishDate.loe(publishDateTo));
        }
    }

    private void addDateCursorCondition(BooleanBuilder builder, Instant cursor,
        boolean isAscending) {
        if (cursor != null) {
            if (isAscending) {
                builder.and(article.publishDate.gt(cursor));
            } else {
                builder.and(article.publishDate.lt(cursor));
            }
        }
    }

    private void addViewCountCursorCondition(BooleanBuilder builder, Long cursorViewCount,
        Instant cursorPublishDate, boolean isAscending) {
        if (cursorViewCount != null && cursorPublishDate != null) {
            if (isAscending) {
                builder.and(
                    article.viewCount.gt(cursorViewCount)
                        .or(article.viewCount.eq(cursorViewCount)
                            .and(article.publishDate.gt(cursorPublishDate))));
            } else {
                builder.and(
                    article.viewCount.lt(cursorViewCount)
                        .or(article.viewCount.eq(cursorViewCount)
                            .and(article.publishDate.lt(cursorPublishDate))));
            }
        }
    }

    private void addCommentCountCursorCondition(BooleanBuilder builder, Long cursorCommentCount,
        Instant cursorPublishDate, boolean isAscending) {
        if (cursorCommentCount != null && cursorPublishDate != null) {
            if (isAscending) {
                builder.and(
                    article.commentCount.gt(cursorCommentCount)
                        .or(article.commentCount.eq(cursorCommentCount)
                            .and(article.publishDate.gt(cursorPublishDate))));
            } else {
                builder.and(
                    article.commentCount.lt(cursorCommentCount)
                        .or(article.commentCount.eq(cursorCommentCount)
                            .and(article.publishDate.lt(cursorPublishDate))));
            }
        }
    }
}
