package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationNotFoundException;
import com.sprint.mission.sb03monewteam1.mapper.NotificationMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.notification.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public void createNewArticleNotification(User user, Interest interest, int articleCount) {

        log.info("구독 알림 등록 요청: userId={}, interestId={}, articleCount={}",
            user.getId(), interest.getId(), articleCount);

        Notification notification = Notification.builder()
            .content(String.format("%s와 관련된 기사가 %d건 등록되었습니다.", interest.getName(), articleCount))
            .resourceType(ResourceType.interest)
            .resourceId(interest.getId())
            .user(user)
            .build();

        notificationRepository.save(notification);

        log.info("구독 알림 등록 완료: userId={}, interestId={}, articleCount={}",
            user.getId(), interest.getId(), articleCount);
    }

    @Override
    public void createCommentLikeNotification(User user, Comment comment) {

        log.info("좋아요 알림 등록 시작: comment={}, likedBy={}, author={}", comment.getId(), user.getId(),
            comment.getAuthor().getId());

        Notification notification = Notification.builder()
            .content(String.format("%s님이 나의 댓글을 좋아합니다.", user.getNickname()))
            .resourceType(ResourceType.comment)
            .resourceId(comment.getId())
            .user(comment.getAuthor())
            .build();

        notificationRepository.save(notification);

        log.info("좋아요 알림 등록 완료: comment={}, likedBy={}, author={}", comment.getId(), user.getId(),
            comment.getAuthor().getId());
    }

    @Override
    public CursorPageResponse<NotificationDto> getUncheckedNotifications(
        UUID userId,
        String cursor,
        Instant after,
        int limit
    ) {
        List<Notification> notifications = notificationRepository
            .findUncheckedNotificationsWithCursor(
                userId,
                cursor,
                after,
                limit + 1
            );

        boolean hasNext = notifications.size() > limit;
        if (hasNext) {
            notifications = notifications.subList(0, limit);
        }

        List<NotificationDto> contents = notifications.stream()
            .map(notificationMapper::toDto)
            .toList();

        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext && !notifications.isEmpty()) {
            Notification lastNotification = notifications.get(notifications.size() - 1);
            nextCursor = lastNotification.getCreatedAt().toString();
            nextAfter = lastNotification.getCreatedAt();
        }

        Long totalElements = notificationRepository.countByUserIdAndIsCheckedFalse(userId);

        return CursorPageResponse.<NotificationDto>builder()
            .content(contents)
            .nextCursor(nextCursor)
            .nextAfter(nextAfter)
            .size(contents.size())
            .totalElements(totalElements)
            .hasNext(hasNext)
            .build();

    }

    @Override
    public NotificationDto confirm(UUID notificationId, UUID requestUserId) {

        log.info("알림 개별 확인 시작: notificationId={}, userId={}", notificationId, requestUserId);

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getUser().getId().equals(requestUserId)) {
            throw new NotificationAccessDeniedException(requestUserId);
        }

        notification.markAsChecked();

        log.info("알림 개별 확인 완료: notificationId={}, userId={}", notificationId, requestUserId);

        return notificationMapper.toDto(notification);
    }
}
