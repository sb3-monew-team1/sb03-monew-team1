package com.sprint.mission.sb03monewteam1.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.QInterest;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QInterest interest = QInterest.interest;

    @Override
    public List<Interest> searchByKeywordOrName(
        String searchKeyword,
        String cursor,
        int limit,
        String sortBy,
        String sortDirection) {

        BooleanBuilder builder = new BooleanBuilder();
        addSearchConditions(builder, searchKeyword);
        addCursorCondition(builder, cursor, sortBy, sortDirection);

        OrderSpecifier<?> orderBy = getOrderSpecifier(sortBy, sortDirection);

        return jpaQueryFactory
            .selectFrom(interest)
            .where(builder)
            .orderBy(orderBy)
            .limit(limit + 1)
            .fetch();
    }

    private void addSearchConditions(BooleanBuilder builder, String searchKeyword) {
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            builder.and(
                interest.name.containsIgnoreCase(searchKeyword)
                    .or(interest.keywords.any().keyword.containsIgnoreCase(searchKeyword))
            );
        }
    }

    private void addCursorCondition(BooleanBuilder builder, String cursor, String sortBy, String sortDirection) {
        if (cursor != null) {
            try {
                if ("subscriberCount".equalsIgnoreCase(sortBy)) {
                    Long cursorSubscriberCount = Long.parseLong(cursor);
                    handleSubscriberCountCursor(builder, cursorSubscriberCount, sortDirection);
                } else if ("name".equalsIgnoreCase(sortBy)) {
                    handleNameCursor(builder, cursor, sortDirection);
                } else {
                    Instant cursorTime = Instant.parse(cursor);
                    handleUpdatedAtCursor(builder, cursorTime, sortDirection);
                }
            } catch (DateTimeParseException | NumberFormatException e) {
            }
        }
    }

    private void handleSubscriberCountCursor(BooleanBuilder builder, Long cursorSubscriberCount, String sortDirection) {
        if ("desc".equalsIgnoreCase(sortDirection)) {
            builder.and(interest.subscriberCount.lt(cursorSubscriberCount));
        } else {
            builder.and(interest.subscriberCount.gt(cursorSubscriberCount));
        }
    }

    private void handleNameCursor(BooleanBuilder builder, String cursor, String sortDirection) {
        if ("desc".equalsIgnoreCase(sortDirection)) {
            builder.and(interest.name.lt(cursor));
        } else {
            builder.and(interest.name.gt(cursor));
        }
    }

    private void handleUpdatedAtCursor(BooleanBuilder builder, Instant cursorTime, String sortDirection) {
        if ("desc".equalsIgnoreCase(sortDirection)) {
            builder.and(interest.updatedAt.lt(cursorTime));
        } else {
            builder.and(interest.updatedAt.gt(cursorTime));
        }
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortBy, String sortDirection) {
        OrderSpecifier<?> orderBy;

        if ("subscriberCount".equalsIgnoreCase(sortBy)) {
            orderBy = new OrderSpecifier<>(
                sortDirection.equalsIgnoreCase("desc") ? com.querydsl.core.types.Order.DESC : com.querydsl.core.types.Order.ASC,
                interest.subscriberCount
            );
        } else if ("name".equalsIgnoreCase(sortBy)) {
            orderBy = new OrderSpecifier<>(
                sortDirection.equalsIgnoreCase("desc") ? com.querydsl.core.types.Order.DESC : com.querydsl.core.types.Order.ASC,
                interest.name
            );
        } else {
            orderBy = new OrderSpecifier<>(com.querydsl.core.types.Order.ASC, interest.updatedAt);
        }

        return orderBy;
    }
}
