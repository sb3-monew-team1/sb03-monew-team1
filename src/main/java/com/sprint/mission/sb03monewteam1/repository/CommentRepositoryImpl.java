package com.sprint.mission.sb03monewteam1.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.QComment;
import com.sprint.mission.sb03monewteam1.exception.article.InvalidCursorException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
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
        where.and(qComment.article.id.eq(articleId));

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
        BooleanBuilder builder = new BooleanBuilder();
        String[] parts = cursor.split("_");
        if (parts.length != 2) {
            throw new InvalidCursorException("잘못된 커서 형식입니다. 커서는 '값_ID' 형식이어야 합니다.");
        }

        String cursorValue = parts[0];
        String cursorIdStr = parts[1];

        UUID cursorId;

        try {
            cursorId = UUID.fromString(cursorIdStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidCursorException("커서의 ID 부분이 유효한 UUID가 아닙니다.");
        }

        if ("createdAt".equals(sortBy)) {
            try {
                Instant cursorCreatedAt = Instant.parse(cursorValue);
                if (direction == Order.ASC) {
                    builder.and(
                        qComment.createdAt.gt(cursorCreatedAt)
                            .or(qComment.createdAt.eq(cursorCreatedAt).and(qComment.id.gt(cursorId)))
                    );
                } else {
                    builder.and(
                        qComment.createdAt.lt(cursorCreatedAt)
                            .or(qComment.createdAt.eq(cursorCreatedAt).and(qComment.id.lt(cursorId)))
                    );
                }
            } catch (DateTimeParseException e) {
                throw new InvalidCursorException("createdAt 정렬 커서의 날짜 형식이 잘못되었습니다.");
            }
        } else if ("likeCount".equals(sortBy)) {
            try {
                Long cursorLikeCount = Long.parseLong(cursorValue);
                if (direction == Order.ASC) {
                    builder.and(
                        qComment.likeCount.gt(cursorLikeCount)
                            .or(
                                qComment.likeCount.eq(cursorLikeCount)
                                    .and(qComment.createdAt.gt(nextAfter)
                                        .or(qComment.createdAt.eq(nextAfter)
                                            .and(qComment.id.gt(cursorId)))
                                    )
                            )
                    );
                } else {
                    builder.and(
                        qComment.likeCount.lt(cursorLikeCount)
                            .or(
                                qComment.likeCount.eq(cursorLikeCount)
                                    .and(qComment.createdAt.lt(nextAfter)
                                        .or(qComment.createdAt.eq(nextAfter)
                                            .and(qComment.id.lt(cursorId)))
                                    )
                            )
                    );
                }
            } catch (NumberFormatException e) {
                throw new InvalidCursorException("likeCount 정렬 커서의 숫자 형식이 잘못되었습니다.");
            }

        }

        return builder;
    }

    private OrderSpecifier<?> createOrderSpecifier(String sortBy, Order direction, QComment qComment) {

        if ("likeCount".equals(sortBy)) {
            return direction == Order.ASC ? qComment.likeCount.asc() : qComment.likeCount.desc();
        }

        return direction == Order.ASC ? qComment.createdAt.asc() : qComment.createdAt.desc();
    }
}
