package com.sprint.mission.sb03monewteam1.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleCollectScheduler {

    private final JobLauncher jobLauncher;
    private final Job articleCollectJob;
    private final JobExplorer jobExplorer;

    @Scheduled(cron = "0 0 * * * *")
    public void runArticleCollectJob() {
        boolean isRunning = jobExplorer.findRunningJobExecutions("articleCollectJob").size() > 0;
        if (isRunning) {
            log.warn("이전 배치 작업이 아직 실행 중이므로 이번 실행은 건너뜁니다.");
            return;
        }
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(articleCollectJob, params);
        } catch (Exception e) {
            log.error("Article 수집 배치 실행 실패", e);
        }
    }
}
