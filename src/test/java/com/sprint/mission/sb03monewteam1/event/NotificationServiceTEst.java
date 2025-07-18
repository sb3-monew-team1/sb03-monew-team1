package com.sprint.mission.sb03monewteam1.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.fixture.NotificationFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.jpa.NotificationRepository;
import com.sprint.mission.sb03monewteam1.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 테스트")
public class NotificationServiceTEst {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    @DisplayName("테스트 환경 설정 확인")
    public void setup() {
        assertNotNull(notificationRepository);
        assertNotNull(notificationService);
    }

    @Nested
    @DisplayName("관심 기사 등록 테스트")
    class InterestRegisterTests {

        @Test
        void 관심_기사_등록_알림을_생성할_수_있다() {
            // Given
            User user = UserFixture.createUser();
            Interest interest = InterestFixture.createInterest();
            Notification notification
                = NotificationFixture.createNewArticleNotification();
            int articleCount = 10;

            given(notificationRepository.save(any(Notification.class))).willReturn(notification);

            // When
            notificationService.createNewArticleNotification(user, interest, articleCount);

            // Then
            then(notificationRepository).should().save(any(Notification.class));
            then(notificationRepository).shouldHaveNoMoreInteractions();
        }
    }


}
