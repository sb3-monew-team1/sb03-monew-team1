package com.sprint.mission.sb03monewteam1.event.listener;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.NewArticleCollectEvent;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationSendException;
import com.sprint.mission.sb03monewteam1.repository.SubscriptionRepository;
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

    @Async
    @EventListener
    public void handleCollectArticle(NewArticleCollectEvent event) {

        Interest interest = event.getInterest();
        List<ArticleDto> articles = event.getArticles();

        log.info("기사 등록 이벤트 요청 - 관심사:{}, 기사 수:{}", interest.getName(), articles.size());

        List<User> subscribers
            = subscriptionRepository.findAllByInterestIdFetchUser(interest.getId())
            .stream()
            .map(Subscription::getUser)
            .toList();

        try {
            log.debug("구독 알림 전송 요청 - 괸삼사:{}, 구독자 수:{}", interest.getName(), subscribers.size());
            subscribers.forEach(
                subscriber -> notificationService.createNewArticleNotification(subscriber, interest, articles.size()));
            log.info("구독 알림 전송 완료 - 관심사:{}, 구독자 수:{}", interest.getName(), subscribers.size());
        } catch (Exception e) {
            log.error("구독 알림 전송 실패 - 관심사:{}, 구독자 수:{}", interest.getName(), subscribers.size());
            throw new NotificationSendException("구독 알림 전송에 실패하였습니다");
        }

    }

}
