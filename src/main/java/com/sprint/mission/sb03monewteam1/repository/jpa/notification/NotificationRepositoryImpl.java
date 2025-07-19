package com.sprint.mission.sb03monewteam1.repository.jpa.notification;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.QNotification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QNotification qNotification = QNotification.notification;

    @Override
    public List<Notification> findUncheckedNotificationsWithCursor(
        UUID userId,
        String cursor,
        Instant nextAfter,
        int limit
    ) {
        BooleanBuilder where = new BooleanBuilder();

        where.and(qNotification.user.id.eq(userId));

        where.and(qNotification.isChecked.isFalse());

        if (cursor != null && !cursor.isBlank()) {
            Instant createdAt = Instant.parse(cursor);
            where.and(qNotification.createdAt.lt(createdAt));
        }

        OrderSpecifier<?> orderSpecifier = qNotification.createdAt.desc();
        OrderSpecifier<?> idOrderSpecifier = qNotification.id.desc();

        return queryFactory
            .selectFrom(qNotification)
            .leftJoin(qNotification).fetchJoin()
            .where(where)
            .orderBy(orderSpecifier, idOrderSpecifier)
            .limit(limit)
            .fetch();
    }
}
