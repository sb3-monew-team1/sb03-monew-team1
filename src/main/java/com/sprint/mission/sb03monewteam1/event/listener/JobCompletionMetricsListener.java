package com.sprint.mission.sb03monewteam1.event.listener;

import com.sprint.mission.sb03monewteam1.config.metric.MonewMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@RequiredArgsConstructor
public class JobCompletionMetricsListener implements JobExecutionListener {

    private final String jobName;
    private final MonewMetrics monewMetrics;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            monewMetrics.recordJobSuccess(jobName);
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            monewMetrics.recordJobFailure(jobName);
        }
    }
}
