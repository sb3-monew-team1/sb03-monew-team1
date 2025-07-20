package com.sprint.mission.sb03monewteam1.scheduler;

import com.sprint.mission.sb03monewteam1.exception.notification.NotificationCleanupException;
import com.sprint.mission.sb03monewteam1.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 15 0 * * *")
    public void runNotificationCleanupBatch() {
        log.info("확인된 알림 삭제 시작");
        try {
            notificationService.deleteOldCheckedNotifications();
        } catch (Exception e) {
            throw new NotificationCleanupException("확인된 알림 삭제에 실패하였습니다.");
        }
    }

}
