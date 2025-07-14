package com.sprint.mission.sb03monewteam1.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.QComment;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final QComment qComment = QComment.comment;

    @Override
    public List<Comment> findCommentsWithCursorBySort(
        UUID articleId,
        String cursor,
        Instant nextAfter,
        int limit,
        String sortBy,
        String sortDirection) {

        Order direction = "ASC".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

        // where 조건 생성을 위한 빌더
        BooleanBuilder where = new BooleanBuilder();
        if (articleId != null) {
            where.and(qComment.article.id.eq(articleId));
        }

        // 커서 조건
        if (cursor != null && !cursor.isBlank()) {
            where.and(createCursorCondition(sortBy, direction, cursor, nextAfter, qComment));
        }

        // 정렬 조건
        OrderSpecifier<?> orderSpecifier = createOrderSpecifier(sortBy, direction, qComment);
        OrderSpecifier<?> idOrderSpecifier = direction == Order.ASC ? qComment.id.asc() : qComment.id.desc();

        return queryFactory
            .selectFrom(qComment)
            .where(where)
            .orderBy(orderSpecifier, idOrderSpecifier)
            .limit(limit)
            .fetch();
    }

    private BooleanBuilder createCursorCondition(String sortBy, Order direction, String cursor, Instant nextAfter, QComment qComment) {

        return switch (sortBy) {
            case "createdAt" -> buildCreatedAtCondition(qComment, direction, cursor);
            case "likeCount" -> buildLikeCountCondition(qComment, direction, cursor, nextAfter);
            default -> throw new InvalidSortOptionException(sortBy);
        };
    }

    private BooleanBuilder buildCreatedAtCondition(QComment q, Order direction, String cursorValue) {
        Instant createdAt = Instant.parse(cursorValue);

        return direction == Order.ASC
            ? new BooleanBuilder(q.createdAt.gt(createdAt))
            : new BooleanBuilder(q.createdAt.lt(createdAt));
    }

    private BooleanBuilder buildLikeCountCondition(QComment q, Order direction, String cursorValue, Instant nextAfter) {
        Long likeCount = Long.parseLong(cursorValue);

        return direction == Order.ASC
            ? new BooleanBuilder(q.likeCount.gt(likeCount).or(q.likeCount.eq(likeCount).and(q.createdAt.gt(nextAfter))))
            : new BooleanBuilder(q.likeCount.lt(likeCount).or(q.likeCount.eq(likeCount).and(q.createdAt.lt(nextAfter))));
    }

    private OrderSpecifier<?> createOrderSpecifier(String sortBy, Order direction, QComment q) {
        return switch (sortBy) {
            case "likeCount" -> direction == Order.ASC ? q.likeCount.asc() : q.likeCount.desc();
            case "createdAt" -> direction == Order.ASC ? q.createdAt.asc() : q.createdAt.desc();
            default -> throw new InvalidSortOptionException(sortBy);
        };
    }
}
