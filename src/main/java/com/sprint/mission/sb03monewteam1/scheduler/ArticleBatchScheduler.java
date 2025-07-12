package com.sprint.mission.sb03monewteam1.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArticleBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job articleJob;

    @Scheduled(cron = "0 0 * * * *")
    public void runArticleJob() throws Exception {
        jobLauncher.run(articleJob, new org.springframework.batch.core.JobParameters());
    }
}
