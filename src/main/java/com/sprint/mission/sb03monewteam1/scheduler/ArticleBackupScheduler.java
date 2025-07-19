package com.sprint.mission.sb03monewteam1.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupScheduler {

    private final JobLauncher jobLauncher;
    private final Job articleBackupJob;

    //    @Scheduled(cron = "0 0 2 * * *")
    @Scheduled(cron = "0 * * * * *")
    public void runArticleBackupJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(articleBackupJob, params);
        } catch (Exception e) {
            log.error("Article 백업 배치 실행 실패", e);
        }
    }
}
