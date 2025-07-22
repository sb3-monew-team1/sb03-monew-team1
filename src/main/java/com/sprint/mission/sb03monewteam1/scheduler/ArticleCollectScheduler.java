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
public class ArticleCollectScheduler {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final Job newsCollectJob;

    //        @Scheduled(cron = "0 0 0 * * *")
    @Scheduled(cron = "*/30 * * * * *")
    public void runNewsCollectJob() {
        runJobIfNotRunning("newsCollectJob", newsCollectJob);
    }

    private void runJobIfNotRunning(String jobName, Job job) {
        if (!jobExplorer.findRunningJobExecutions(jobName).isEmpty()) {
            log.warn("{}이(가) 아직 실행 중이므로 이번 실행은 건너뜁니다.", jobName);
            return;
        }

        if (restartPreviousExecution(jobName)) {
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

    private boolean restartPreviousExecution(String jobName) {
        var instances = jobExplorer.getJobInstances(jobName, 0, 1);
        if (!instances.isEmpty()) {
            var executions = jobExplorer.getJobExecutions(instances.get(0));
            for (JobExecution execution : executions) {
                if (execution.getStatus() == BatchStatus.FAILED
                    || execution.getStatus() == BatchStatus.STOPPED) {
                    try {
                        jobOperator.restart(execution.getId());
                        return true;
                    } catch (Exception e) {
                        log.error("{} 재시작 실패", jobName, e);
                    }
                }
            }
        }
        return false;
    }
}
