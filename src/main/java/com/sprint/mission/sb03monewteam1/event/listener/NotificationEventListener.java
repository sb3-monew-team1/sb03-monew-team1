package com.sprint.mission.sb03monewteam1.event.listener;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.CommentLikeEvent;
import com.sprint.mission.sb03monewteam1.event.NewArticleCollectEvent;
import com.sprint.mission.sb03monewteam1.event.NewsCollectJobCompletedEvent;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationSendException;
import com.sprint.mission.sb03monewteam1.repository.jpa.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    private final SubscriptionRepository subscriptionRepository;

    private final java.util.Map<java.util.UUID, Integer> articleCountMap = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<java.util.UUID, String> interestNameMap = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Set<String> completedJobs = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleCollectArticle(NewArticleCollectEvent event) {

        java.util.UUID interestId = event.getInterestId();
        String interestName = event.getInterestName();
        List<ArticleDto> articles = event.getArticles();

        log.info("기사 등록 이벤트 요청 - 관심사:{}, 기사 수:{}", interestName, articles.size());

        articleCountMap.merge(interestId, articles.size(), Integer::sum);
        interestNameMap.putIfAbsent(interestId, interestName);

    }

    @EventListener
    public void handleCollectJobCompleted(NewsCollectJobCompletedEvent event) {
        completedJobs.add(event.getJobName());
        if (completedJobs.contains("naverNewsCollectJob") && completedJobs.contains(
            "hankyungNewsCollectJob")) {
            flushNotifications();
            articleCountMap.clear();
            interestNameMap.clear();
            completedJobs.clear();
        }
    }

    private void flushNotifications() {
        for (java.util.Map.Entry<java.util.UUID, Integer> entry : articleCountMap.entrySet()) {
            java.util.UUID interestId = entry.getKey();
            int count = entry.getValue();
            String interestName = interestNameMap.getOrDefault(interestId, "");

            List<User> subscribers = subscriptionRepository.findAllByInterestIdFetchUser(interestId)
                .stream()
                .map(Subscription::getUser)
                .filter(user -> !user.isDeleted())
                .toList();

            try {
                log.info("구독 알림 전송 요청 - 관심사:{}, 구독자 수:{}, 기사 수:{}", interestName,
                    subscribers.size(), count);
                subscribers.forEach(subscriber -> {
                    Interest interest = Interest.builder()
                        .name(interestName)
                        .build();
                    interest.setIdForTest(interestId);
                    notificationService.createNewArticleNotification(subscriber, interest, count);
                });
                log.info("구독 알림 전송 완료 - 관심사:{}, 구독자 수:{}", interestName, subscribers.size());
            } catch (Exception e) {
                log.error("구독 알림 전송 실패 - 관심사:{}, 구독자 수:{}", interestName, subscribers.size());
                throw new NotificationSendException("구독 알림 전송에 실패하였습니다");
            }
        }
    }

    @Async
    @EventListener
    public void handleCommentLike(CommentLikeEvent event) {

        User user = event.getUser();
        Comment comment = event.getComment();
        User author = comment.getAuthor();

        log.info("좋아요 등록 이벤트 요청 - comment={}, likedBy={}, author={}", comment.getId(),
            user.getNickname(), author.getId());

        try {
            log.debug("좋아요 알림 전송 요청 - comment={}, likedBy={}, author={}", comment.getId(),
                user.getNickname(), author.getId());
            notificationService.createCommentLikeNotification(user, comment);
            log.info("좋아요 알림 전송 완료 - comment={}, likedBy={}, author={}", comment.getId(),
                user.getNickname(), author.getId());
        } catch (Exception e) {
            log.error("좋아요 알림 전송 실패: {}", e.getMessage(), e);
            throw new NotificationSendException("좋아요 알림 전송에 실패하였습니다.");
        }
    }
}
