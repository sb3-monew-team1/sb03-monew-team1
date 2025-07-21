package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestUpdateRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.SubscriptionActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestSimilarityException;
import com.sprint.mission.sb03monewteam1.exception.interest.SubscriptionNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.mapper.InterestMapper;
import com.sprint.mission.sb03monewteam1.mapper.SubscriptionMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.interest.InterestKeywordRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.interest.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.subscription.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.user.UserRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InterestKeywordRepository interestKeywordRepository;
    private final UserRepository userRepository;

    private final InterestMapper interestMapper;
    private final SubscriptionMapper subscriptionMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public InterestDto create(InterestRegisterRequest request) {
        log.info("새로운 관심사 등록 요청: {}", request);

        if (interestRepository.existsByName(request.name())) {
            log.warn("중복된 관심사 이름: {}", request.name());
            throw new InterestDuplicateException(request.name());
        }

        String similarInterestName = findSimilarInterestName(request.name());
        if (similarInterestName != null) {
            log.warn("유사한 관심사 이름: {}", similarInterestName);
            throw new InterestSimilarityException(similarInterestName);
        }

        Interest interest = Interest.builder()
            .name(request.name())
            .build();

        for (String keyword : request.keywords()) {
            InterestKeyword interestKeyword = InterestKeyword.builder()
                .interest(interest)
                .keyword(keyword)
                .build();
            interest.getKeywords().add(interestKeyword);
        }

        Interest saved = interestRepository.save(interest);

        InterestDto response = interestMapper.toDto(saved, false);
        log.info("관심사 등록 완료: {}", response);

        return response;
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<InterestDto> getInterests(
        UUID userId, String keyword, String cursor, int limit, String orderBy, String direction) {

        log.info("관심사 조회 요청: keyword={}, cursor={}, limit={}, orderBy={}, direction={}",
            keyword, cursor, limit, orderBy, direction);

        if (!isValidSortOption(orderBy)) {
            log.error("관심사 조회 요청: 잘못된 정렬 기준: orderBy={}", orderBy);
            throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD, "sortBy", orderBy);
        }

        if (!isValidSortDirection(direction)) {
            log.error("관심사 조회 요청: 잘못된 정렬 방향: direction={}", direction);
            throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_DIRECTION, "sortDirection",
                direction);
        }

        if (cursor != null && !cursor.trim().isEmpty() && !isValidCursor(cursor, orderBy)) {
            log.error("잘못된 커서 형식: cursor={}, orderBy={}", cursor, orderBy);
            throw new InvalidCursorException(ErrorCode.INVALID_CURSOR_FORMAT, cursor);
        }

        String cursorValue = cursor != null && !cursor.trim().isEmpty() ? cursor : null;

        List<Interest> interests = interestRepository.searchByKeywordOrName(
            keyword, cursorValue, limit + 1, orderBy, direction);

        Set<UUID> subscribedInterestIds = new HashSet<>();
        subscriptionRepository.findAllByUserId(userId)
            .forEach(subscription -> subscribedInterestIds.add(subscription.getInterest().getId()));

        List<InterestDto> content = interests.stream()
            .map(interest -> {
                boolean isSubscribed = subscribedInterestIds.contains(interest.getId());

                return InterestDto.builder()
                    .id(interest.getId())
                    .name(interest.getName())
                    .keywords(
                        interest.getKeywords() != null
                            ? interest.getKeywords().stream()
                            .map(InterestKeyword::getKeyword)
                            .collect(Collectors.toList())
                            : Collections.emptyList()
                    )
                    .subscriberCount(interest.getSubscriberCount())
                    .subscribedByMe(isSubscribed)
                    .build();
            })
            .collect(Collectors.toList());

        String nextCursor = calculateNextCursor(interests, orderBy, limit);
        Instant nextAfter = calculateNextAfter(interests);

        boolean hasNext = interests.size() > limit;

        if (hasNext) {
            content = content.subList(0, limit);
        }

        long totalElements = interestRepository.countByKeywordOrName(keyword);

        return new CursorPageResponse<>(content, nextCursor, nextAfter, limit, totalElements,
            hasNext);
    }

    @Transactional
    public SubscriptionDto createSubscription(UUID userId, UUID interestId) {
        log.info("구독 생성 요청: userId={}, interestId={}", userId, interestId);

        Interest interest = interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        log.info("현재 관심사 구독자 수: {}", interest.getSubscriberCount());

        interest.setSubscriberCount(interest.getSubscriberCount() + 1);
        log.info("구독자 수 증가 후: {}", interest.getSubscriberCount());

        Subscription subscription = Subscription.builder()
            .user(user)
            .interest(interest)
            .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        log.info("구독 생성 완료: subscriptionId={}, userId={}, interestId={}",
            savedSubscription.getId(), user.getId(), interest.getId());

        SubscriptionDto eventDto = subscriptionMapper.toDto(savedSubscription);
        eventPublisher.publishEvent(new SubscriptionActivityCreateEvent(userId, eventDto));
        log.debug("구독 활동 내역 이벤트 발행 완료: {}", eventDto);

        return subscriptionMapper.toDto(savedSubscription);
    }

    @Override
    public InterestDto updateInterestKeywords(UUID interestId, InterestUpdateRequest request,
        UUID userId) {
        log.info("관심사 수정 요청: userId={}, interestId={}, request={}", userId, interestId, request);

        Interest interest = interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));

        List<String> newKeywords = request.keywords();

        interest.getKeywords().clear();

        for (String keyword : newKeywords) {
            InterestKeyword interestKeyword = InterestKeyword.builder()
                .keyword(keyword)
                .interest(interest)
                .build();
            interest.getKeywords().add(interestKeyword);
        }

        boolean subscribedByMe = subscriptionRepository.existsByUserIdAndInterestId(userId,
            interestId);

        Interest updatedInterest = interestRepository.save(interest);

        InterestDto updatedInterestDto = interestMapper.toDto(updatedInterest, subscribedByMe);

        log.info("관심사 수정 완료: response={}", updatedInterestDto);

        return updatedInterestDto;
    }


    @Override
    public void deleteInterest(UUID interestId) {
        log.info("관심사 삭제 요청: interestId={}", interestId);
        Interest interest = interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));

        subscriptionRepository.deleteByInterestId(interestId);

        interestKeywordRepository.deleteByInterestId(interestId);

        interestRepository.delete(interest);

        log.info("관심사 삭제 완료: interestId={}", interestId);
    }

    @Override
    public void deleteSubscription(UUID userId, UUID interestId) {
        log.info("구독 취소 요청: userId={}, interestId={}", userId, interestId);

        Interest interest = interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Subscription subscription = subscriptionRepository.findByUserIdAndInterestId(userId,
                interestId)
            .orElseThrow(() -> new SubscriptionNotFoundException(userId, interestId));

        subscriptionRepository.delete(subscription);
        interest.setSubscriberCount(Math.max(0, interest.getSubscriberCount() - 1));

        log.info("구독 취소 완료: subscriptionId={}, userId={}, interestId={}, 남은 구독자 수={}",
            subscription.getId(), userId, interestId, interest.getSubscriberCount());
    }

    private String calculateNextCursor(List<Interest> interests, String orderBy, int limit) {
        if (interests.size() <= limit) {
            return null;
        }

        Interest lastInterest = interests.get(limit - 1);

        String cursorValue = "";
        switch (orderBy) {
            case "subscriberCount":
                cursorValue = String.valueOf(lastInterest.getSubscriberCount());
                break;
            case "name":
                cursorValue = lastInterest.getName();
                break;
            case "createdAt":
                cursorValue = lastInterest.getCreatedAt().toString();
                break;
            case "updatedAt":
                cursorValue = lastInterest.getUpdatedAt().toString();
                break;
            default:
                throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD, "orderBy",
                    orderBy);
        }
        return cursorValue;
    }

    private Instant calculateNextAfter(List<Interest> interests) {
        if (!interests.isEmpty()) {
            return interests.get(interests.size() - 1).getCreatedAt();
        }
        return null;
    }

    private String findSimilarInterestName(String newInterestName) {
        for (Interest existingInterest : interestRepository.findAll()) {
            double similarity = calculateSimilarity(existingInterest.getName(), newInterestName);
            if (similarity >= 0.8) {
                return existingInterest.getName();
            }
        }
        return null;
    }

    private double calculateSimilarity(String name1, String name2) {
        LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();
        int distance = levenshtein.apply(name1, name2);
        int maxLength = Math.max(name1.length(), name2.length());
        double similarity = 1.0 - ((double) distance / maxLength);
        log.debug("유사도 계산: name1={}, name2={}, similarity={}", name1, name2, similarity);
        return similarity;
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
                    if (cursor.length() > 255) {
                        return false;
                    }
                    break;
                case "createdAt":
                case "updatedAt":
                    try {
                        Instant.parse(cursor);
                    } catch (Exception e) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidSortOption(String orderBy) {
        List<String> validFields = List.of("subscriberCount", "name", "createdAt", "updatedAt");
        return validFields.contains(orderBy);
    }

    private boolean isValidSortDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) || "DESC".equalsIgnoreCase(direction);
    }
}
