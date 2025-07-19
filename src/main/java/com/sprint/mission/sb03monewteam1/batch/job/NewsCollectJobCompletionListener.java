package com.sprint.mission.sb03monewteam1.batch.job;

import com.sprint.mission.sb03monewteam1.event.NewsCollectJobCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public class NewsCollectJobCompletionListener implements JobExecutionListener {

    private final ApplicationEventPublisher eventPublisher;
    private final String jobName;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            eventPublisher.publishEvent(new NewsCollectJobCompletedEvent(jobName));
        }
    }
}
