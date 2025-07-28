package com.sprint.mission.sb03monewteam1.scheduler;

import com.sprint.mission.sb03monewteam1.exception.notification.NotificationCleanupException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job deleteOldNotificationsJob;

    @Scheduled(cron = "0 15 0 * * *")
    public void runNotificationCleanupBatch() {
        log.info("스케줄러: 알림 삭제 배치 잡 실행 요청");
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(deleteOldNotificationsJob, params);
        } catch (Exception e) {
            log.error("배치 잡 실행 실패", e);
            throw new NotificationCleanupException("알림 삭제 배치 실행 실패.");
        }
    }

}
