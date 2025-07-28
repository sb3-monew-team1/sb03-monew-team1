package com.sprint.mission.sb03monewteam1.event.listener;

import com.sprint.mission.sb03monewteam1.config.metric.MonewMetrics;
import com.sprint.mission.sb03monewteam1.event.NewsCollectJobCompletedEvent;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public class NewsCollectJobCompletionListener implements JobExecutionListener {

    private final ApplicationEventPublisher eventPublisher;
    private final MonewMetrics monewMetrics;
    private final String jobName;

    private static final String START_TIME_KEY = "jobStartTime";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext().putLong(START_TIME_KEY, System.currentTimeMillis());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long startTime = jobExecution.getExecutionContext().getLong(START_TIME_KEY, -1L);
        long duration = (startTime > 0) ? System.currentTimeMillis() - startTime : 0;

        boolean success = jobExecution.getStatus() == BatchStatus.COMPLETED;
        eventPublisher.publishEvent(new NewsCollectJobCompletedEvent(jobName));

        if (success) {
            monewMetrics.recordJobSuccess(jobName);
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            monewMetrics.recordJobFailure(jobName);
        }

        if (duration > 0) {
            Timer timer = monewMetrics.getBatchJobTimer(jobName);
            timer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }
}
