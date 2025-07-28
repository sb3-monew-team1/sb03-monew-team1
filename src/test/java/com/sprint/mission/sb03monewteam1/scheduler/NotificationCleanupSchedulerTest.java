package com.sprint.mission.sb03monewteam1.scheduler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

import com.sprint.mission.sb03monewteam1.exception.notification.NotificationCleanupException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

@ExtendWith(MockitoExtension.class)
public class NotificationCleanupSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job deleteOldNotificationsJob;

    @InjectMocks
    private NotificationCleanupScheduler scheduler;

    @Test
    void 알림_정리_배치_작업이_실행되면_알림_삭제_서비스가_실행되어야_한다()
        throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // When
        scheduler.runNotificationCleanupBatch();

        // Then
        then(jobLauncher).should(times(1)).run(any(), any());
    }

    @Test
    void 알림_정리_배치_작업_중_오류가_발생하면_500이_반환되어야_한다()
        throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // Given
        doThrow(new NotificationCleanupException("확인된 알림 삭제에 실패하였습니다.")).when(jobLauncher)
            .run(any(), any());

        // When & Then
        NotificationCleanupException ex = assertThrows(NotificationCleanupException.class, () -> {
            scheduler.runNotificationCleanupBatch();
        });

        assertThat(ex.getMessage()).isEqualTo("확인된 알림 삭제에 실패하였습니다.");
        assertThat(ex.getErrorCode().getHttpStatus().toString())
            .isEqualTo("500 INTERNAL_SERVER_ERROR");
    }
}
