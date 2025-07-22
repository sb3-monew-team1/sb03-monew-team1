package com.sprint.mission.sb03monewteam1.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class LogBackupSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job logBackupJob;

    @InjectMocks
    private LogBackupScheduler scheduler;

    @Test
    void 로그_백업_배치_작업이_실행되면_jobLauncher가_호출되어야_한다() throws Exception {
        scheduler.runLogBackupJob();
        then(jobLauncher).should(times(1)).run(any(), any());
    }
}
