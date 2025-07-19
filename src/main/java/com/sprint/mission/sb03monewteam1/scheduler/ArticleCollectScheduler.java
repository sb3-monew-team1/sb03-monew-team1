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
    private final JobExplorer jobExplorer;
    private final Job naverNewsCollectJob;
    private final Job hankyungNewsCollectJob;

    @Scheduled(cron = "0 0 * * * *")
    public void runNaverNewsCollectJob() {
        runJobIfNotRunning("naverNewsCollectJob", naverNewsCollectJob);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void runHankyungNewsCollectJob() {
        runJobIfNotRunning("hankyungNewsCollectJob", hankyungNewsCollectJob);
    }

    private void runJobIfNotRunning(String jobName, Job job) {
        boolean isRunning = jobExplorer.findRunningJobExecutions(jobName).size() > 0;
        if (isRunning) {
            log.warn("{}이(가) 아직 실행 중이므로 이번 실행은 건너뜁니다.", jobName);
            return;
        }
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(job, params);
        } catch (Exception e) {
            log.error("{} 실행 실패", jobName, e);
        }
    }
}
