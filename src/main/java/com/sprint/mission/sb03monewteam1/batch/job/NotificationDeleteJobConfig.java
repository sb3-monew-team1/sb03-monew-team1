package com.sprint.mission.sb03monewteam1.batch.job;

import com.sprint.mission.sb03monewteam1.service.NotificationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationDeleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationService notificationService;

    @Bean
    public Job deleteOldNotificationsJob() {
        return new JobBuilder("deleteOldNotificationsJob", jobRepository)
            .start(deleteOldNotificationsStep())
            .build();
    }

    @Bean
    public Step deleteOldNotificationsStep() {
        return new StepBuilder("deleteOldNotificationsStep", jobRepository)
            .tasklet(deleteOldNotificationsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet deleteOldNotificationsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Tasklet 시작 - 알림 삭제 서비스 호출");
            notificationService.deleteOldCheckedNotifications();
            log.info("Tasklet 종료 - 알림 삭제 서비스 호출");
            return RepeatStatus.FINISHED;
        };
    }

}
