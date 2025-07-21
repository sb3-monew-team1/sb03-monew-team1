package com.sprint.mission.sb03monewteam1.scheduler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

import com.sprint.mission.sb03monewteam1.exception.notification.NotificationCleanupException;
import com.sprint.mission.sb03monewteam1.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationCleanupSchedulerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationCleanupScheduler scheduler;

    @Test
    void 알림_정리_배치_작업이_실행되면_알림_삭제_서비스가_실행되어야_한다() {
        // When
        scheduler.runNotificationCleanupBatch();

        // Then
        then(notificationService).should(times(1)).deleteOldCheckedNotifications();
    }

    @Test
    void 알림_정리_배치_작업_중_오류가_발생하면_500이_반환되어야_한다() {
        // Given
        doThrow(new NotificationCleanupException("확인된 알림 삭제에 실패하였습니다.")).when(notificationService)
            .deleteOldCheckedNotifications();

        // When & Then
        NotificationCleanupException ex = assertThrows(NotificationCleanupException.class, () -> {
            scheduler.runNotificationCleanupBatch();
        });

        assertThat(ex.getMessage()).isEqualTo("확인된 알림 삭제에 실패하였습니다.");
        assertThat(ex.getErrorCode().getHttpStatus().toString())
            .isEqualTo("500 INTERNAL_SERVER_ERROR");
    }
}
