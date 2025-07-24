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

@Slf4j
@Component
@RequiredArgsConstructor
public class LogBackupScheduler {

    private final JobLauncher jobLauncher;
    private final Job logBackupJob;
    private final JobExplorer jobExplorer;

    @Scheduled(cron = "0 0 3 * * *")
    public void runLogBackupJob() {
        if (!jobExplorer.findRunningJobExecutions("logBackupJob").isEmpty()) {
            log.warn("로그 백업 작업이 아직 실행 중이므로 이번 실행은 건너뜁니다.");
            return;
        }

        log.info("스케줄러: 로그 백업 배치 잡 실행 요청");

        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(logBackupJob, params);
        } catch (Exception e) {
            log.error("로그 백업 배치 실행 실패", e);
        }
    }
}
