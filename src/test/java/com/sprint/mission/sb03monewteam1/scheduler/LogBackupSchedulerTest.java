package com.sprint.mission.sb03monewteam1.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class LogBackupSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job logBackupJob;

    @Mock
    private JobExplorer jobExplorer;

    @InjectMocks
    private LogBackupScheduler scheduler;

    @Test
    void 로그_백업_배치_작업이_실행되면_jobLauncher가_호출되어야_한다() throws Exception {
        org.mockito.BDDMockito.given(jobExplorer.findRunningJobExecutions("logBackupJob"))
            .willReturn(Collections.emptySet());

        scheduler.runLogBackupJob();
        then(jobLauncher).should(times(1)).run(any(), any());
    }

    @Test
    void 이미_실행중이면_jobLauncher가_호출되지_않는다() throws Exception {
        given(jobExplorer.findRunningJobExecutions("logBackupJob"))
            .willReturn(
                Collections.singleton(mock(org.springframework.batch.core.JobExecution.class)));

        scheduler.runLogBackupJob();

        verify(jobLauncher, never()).run(any(), any());
    }

    @Test
    void jobLauncher_실행중_예외발생해도_예외_던지지_않는다() throws Exception {
        given(jobExplorer.findRunningJobExecutions("logBackupJob"))
            .willReturn(Collections.emptySet());
        willThrow(new RuntimeException("실패")).given(jobLauncher).run(any(), any());

        assertDoesNotThrow(() -> scheduler.runLogBackupJob());
    }
}
