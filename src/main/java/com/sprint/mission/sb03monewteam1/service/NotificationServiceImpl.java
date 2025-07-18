package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;

    @Override
    public void createNewArticleNotification(User user, Interest interest, int articleCount) {

        log.info("구독 알림 등록 요청: user={}, interest={}, articleCount={}", user, interest, articleCount);

        Notification notification = Notification.builder()
            .content(String.format("%s와 관련된 기사가 %d건 등록되었습니다.", interest.getName(), articleCount))
            .resourceType(ResourceType.interest)
            .resourceId(interest.getId())
            .user(user)
            .build();

        Notification savedNotification = notificationRepository.save(notification);

        log.info("구독 알림 등록 완료: user={}, interest={}, articleCount={}",
            savedNotification.getUser(), interest, articleCount);
    }

    @Override
    public void createCommentLikeNotification(User user, Comment comment) {

    }
}
