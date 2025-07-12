package com.sprint.mission.sb03monewteam1.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.QArticle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QArticle article = QArticle.article;

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
            Long cursor,
            int limit,
            boolean isAscending) {

        BooleanBuilder builder = createBaseCondition();
        addSearchConditions(builder, keyword, sourceIn, publishDateFrom, publishDateTo);
        addViewCountCursorCondition(builder, cursor, isAscending);

        OrderSpecifier<?>[] orderBy = isAscending
                ? new OrderSpecifier[] { article.viewCount.asc(), article.publishDate.asc() }
                : new OrderSpecifier[] { article.viewCount.desc(), article.publishDate.desc() };

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
            Long cursor,
            int limit,
            boolean isAscending) {

        BooleanBuilder builder = createBaseCondition();
        addSearchConditions(builder, keyword, sourceIn, publishDateFrom, publishDateTo);
        addCommentCountCursorCondition(builder, cursor, isAscending);

        OrderSpecifier<?>[] orderBy = isAscending
                ? new OrderSpecifier[] { article.commentCount.asc(), article.publishDate.asc() }
                : new OrderSpecifier[] { article.commentCount.desc(), article.publishDate.desc() };

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

    private void addDateCursorCondition(BooleanBuilder builder, Instant cursor, boolean isAscending) {
        if (cursor != null) {
            if (isAscending) {
                builder.and(article.publishDate.gt(cursor));
            } else {
                builder.and(article.publishDate.lt(cursor));
            }
        }
    }

    private void addViewCountCursorCondition(BooleanBuilder builder, Long cursor, boolean isAscending) {
        if (cursor != null) {
            if (isAscending) {
                builder.and(article.viewCount.gt(cursor));
            } else {
                builder.and(article.viewCount.lt(cursor));
            }
        }
    }

    private void addCommentCountCursorCondition(BooleanBuilder builder, Long cursor, boolean isAscending) {
        if (cursor != null) {
            if (isAscending) {
                builder.and(article.commentCount.gt(cursor));
            } else {
                builder.and(article.commentCount.lt(cursor));
            }
        }
    }
}
