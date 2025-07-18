package com.sprint.mission.sb03monewteam1.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.listener.NotificationEventListener;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationSendException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.fixture.SubscriptionFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListenerService 테스트")
public class NotificationEventListenerServiceTEst {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @Nested
    @DisplayName("관심 기사 알림 이벤트 리스너 테스트")
    class InterestNotificationEventListenerTests {

        @Test
        void 관심_기사_등록_이벤트_밠생_시_알림이_생성된다() {
            // Given
            Interest interest = InterestFixture.createInterest();
            User user = UserFixture.createUser();
            List<ArticleDto> articles = ArticleFixture.createArticleDtoList();
            List<Subscription> subscription = List.of(SubscriptionFixture.createSubscription(user, interest));
            NewArticleCollectEvent event = new NewArticleCollectEvent(interest, articles);

            given(subscriptionRepository.findAllByInterestIdFetchUser(interest.getId())).willReturn(subscription);

            // When
            listener.handleCollectArticle(event);

            // Then
            then(subscriptionRepository).should().findAllByInterestIdFetchUser(interest.getId());
            then(notificationService).should()
                .createNewArticleNotification(any(User.class), any(Interest.class), any(Integer.class));

        }

        @Test
        void 관심_기사_등록_이벤트_발생_후_알림_전송에_실패_시_예외가_발생한다() {
            // Given
            Interest interest = InterestFixture.createInterest();
            User user = UserFixture.createUser();
            List<ArticleDto> articles = ArticleFixture.createArticleDtoList();
            List<Subscription> subscription = List.of(SubscriptionFixture.createSubscription(user, interest));
            NewArticleCollectEvent event = new NewArticleCollectEvent(interest, articles);

            given(subscriptionRepository.findAllByInterestIdFetchUser(interest.getId())).willReturn(subscription);

            doThrow(new RuntimeException("강제 실패")).when(notificationService)
                .createNewArticleNotification(any(), any(), anyInt());

            // When & Then
            assertThatThrownBy(() -> listener.handleCollectArticle(event))
                .isInstanceOf(NotificationSendException.class);

            then(subscriptionRepository).should().findAllByInterestIdFetchUser(interest.getId());
        }
    }

}
