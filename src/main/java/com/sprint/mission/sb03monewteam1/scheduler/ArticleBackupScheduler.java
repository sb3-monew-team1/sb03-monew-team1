package com.sprint.mission.sb03monewteam1.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupScheduler {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final Job articleBackupJob;

    @Scheduled(cron = "0 0 2 * * *")
    public void runArticleBackupJob() {
        if (!jobExplorer.findRunningJobExecutions("articleBackupJob").isEmpty()) {
            log.warn("articleBackupJob이 아직 실행 중입니다.");
            return;
        }
        log.info("스케줄러: 기사 백업 배치 잡 실행 요청");

        if (restartPreviousExecution()) {
            return;
        }

        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(articleBackupJob, params);
        } catch (Exception e) {
            log.error("Article 백업 배치 실행 실패", e);
        }
    }

    private boolean restartPreviousExecution() {
        var instances = jobExplorer.getJobInstances("articleBackupJob", 0, 1);
        if (!instances.isEmpty()) {
            var executions = jobExplorer.getJobExecutions(instances.get(0));
            for (JobExecution execution : executions) {
                if (execution.getStatus() == BatchStatus.FAILED
                    || execution.getStatus() == BatchStatus.STOPPED) {
                    try {
                        jobOperator.restart(execution.getId());
                        return true;
                    } catch (Exception e) {
                        log.error("articleBackupJob 재시작 실패", e);
                    }
                }
            }
        }
        return false;
    }
}
