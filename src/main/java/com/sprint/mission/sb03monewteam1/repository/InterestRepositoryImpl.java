package com.sprint.mission.sb03monewteam1.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.QInterest;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QInterest qInterest = QInterest.interest;

    @Override
    public List<Interest> searchByKeywordOrName(
        String keyword,
        String cursor,
        int limit,
        String orderBy,
        String direction) {

        Order directionEnum = "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC;

        if (!isValidSortOption(orderBy)) {
            throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD, "sortBy", orderBy);
        }

        BooleanBuilder builder = new BooleanBuilder();
        addSearchConditions(builder, keyword);

        if (cursor != null && !cursor.isBlank()) {
            if (!isValidCursor(cursor, orderBy)) {
                throw new InvalidCursorException(ErrorCode.INVALID_CURSOR_FORMAT, cursor);
            }
            builder.and(createCursorCondition(orderBy, directionEnum, cursor));
        }

        OrderSpecifier<?> orderSpecifier = createOrderSpecifier(orderBy, directionEnum);
        OrderSpecifier<?> idOrderSpecifier = directionEnum == Order.ASC ? qInterest.id.asc() : qInterest.id.desc();

        List<Interest> result = jpaQueryFactory
            .selectFrom(qInterest)
            .where(builder)
            .orderBy(orderSpecifier, idOrderSpecifier)
            .limit(limit + 1)
            .fetch();

        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        return result;
    }

    @Override
    public long countByKeywordOrName(String keyword) {
        QInterest qInterest = QInterest.interest;

        BooleanBuilder builder = new BooleanBuilder();
        addSearchConditions(builder, keyword);

        return jpaQueryFactory
            .selectFrom(qInterest)
            .where(builder)
            .fetchCount();
    }

    private void addSearchConditions(BooleanBuilder builder, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(
                qInterest.name.containsIgnoreCase(keyword)
                    .or(qInterest.keywords.any().keyword.containsIgnoreCase(keyword))
            );
        }
    }

    private BooleanBuilder createCursorCondition(String orderBy, Order direction, String cursor) {
        BooleanBuilder builder = new BooleanBuilder();

        if ("subscriberCount".equalsIgnoreCase(orderBy)) {
            Long cursorSubscriberCount = Long.parseLong(cursor);
            handleSubscriberCountCursor(builder, cursorSubscriberCount, direction);
        } else if ("name".equalsIgnoreCase(orderBy)) {
            handleNameCursor(builder, cursor, direction);
        } else {
            throw new InvalidSortOptionException(orderBy);
        }

        return builder;
    }

    private void handleSubscriberCountCursor(BooleanBuilder builder, Long cursorSubscriberCount, Order direction) {
        if ("desc".equalsIgnoreCase(direction.name())) {
            builder.and(qInterest.subscriberCount.lt(cursorSubscriberCount));
        } else {
            builder.and(qInterest.subscriberCount.gt(cursorSubscriberCount));
        }
    }

    private void handleNameCursor(BooleanBuilder builder, String cursor, Order direction) {
        if ("desc".equalsIgnoreCase(direction.name())) {
            builder.and(qInterest.name.lt(cursor));
        } else {
            builder.and(qInterest.name.gt(cursor));
        }
    }

    private OrderSpecifier<?> createOrderSpecifier(String orderBy, Order direction) {
        OrderSpecifier<?> orderBySpecifier;

        if ("subscriberCount".equalsIgnoreCase(orderBy)) {
            orderBySpecifier = new OrderSpecifier<>(direction.equals(Order.DESC) ? Order.DESC : Order.ASC, qInterest.subscriberCount);
        } else if ("name".equalsIgnoreCase(orderBy)) {
            orderBySpecifier = new OrderSpecifier<>(direction.equals(Order.DESC) ? Order.DESC : Order.ASC, qInterest.name);
        } else if ("createdAt".equalsIgnoreCase(orderBy)) {
            orderBySpecifier = new OrderSpecifier<>(direction.equals(Order.DESC) ? Order.DESC : Order.ASC, qInterest.createdAt);
        } else if ("updatedAt".equalsIgnoreCase(orderBy)) {
            orderBySpecifier = new OrderSpecifier<>(direction.equals(Order.DESC) ? Order.DESC : Order.ASC, qInterest.updatedAt);
        } else {
            throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD, "orderBy", orderBy);
        }

        return orderBySpecifier;
    }

    private boolean isValidCursor(String cursor, String orderBy) {
        try {
            switch (orderBy) {
                case "subscriberCount":
                    Long.parseLong(cursor);
                    break;
                case "name":
                    if (cursor == null || cursor.trim().isEmpty()) {
                        return false;
                    }
                    break;
                default:
                    throw new InvalidSortOptionException(orderBy);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidSortOption(String orderBy) {
        List<String> validFields = List.of("subscriberCount", "name", "createdAt", "updatedAt");
        return validFields.contains(orderBy);
    }
}
