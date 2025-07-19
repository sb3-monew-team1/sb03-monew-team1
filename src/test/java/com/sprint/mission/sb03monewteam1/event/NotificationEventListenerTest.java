package com.sprint.mission.sb03monewteam1.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.listener.NotificationEventListener;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationSendException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.fixture.SubscriptionFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.jpa.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.service.NotificationService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListener 테스트")
public class NotificationEventListenerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {

    }

    @Nested
    @DisplayName("좋아요 알림 이벤트 리스너 테스트")
    class CommentLikeNotificationListenerTest {

        @Test
        void 댓글_좋아요_이벤트_발생시_알림이_생성된다() {

            // given
            User user = UserFixture.createUser();
            Comment comment = CommentFixture.createComment("좋아요 이벤트 리스너 테스트", user,
                ArticleFixture.createArticle());
            CommentLikeEvent event = new CommentLikeEvent(user, comment);

            // when
            listener.handleCommentLike(event);

            // then
            verify(notificationService).createCommentLikeNotification(user, comment);
        }
    }

    @Nested
    @DisplayName("관심 기사 알림 이벤트 리스너 테스트")
    class InterestNotificationEventListenerTests {

        @Test
        void 관심_기사_등록_이벤트_밠생_시_알림이_생성된다() {
            // Given
            Interest interest = InterestFixture.createInterestWithId();
            User user = UserFixture.createUser();
            List<ArticleDto> articles = ArticleFixture.createArticleDtoList();
            List<Subscription> subscription = List.of(
                SubscriptionFixture.createSubscription(user, interest));
            NewArticleCollectEvent event = new NewArticleCollectEvent(
                interest.getId(), interest.getName(), articles);

            given(subscriptionRepository.findAllByInterestIdFetchUser(interest.getId())).willReturn(
                subscription);

            // When
            listener.handleCollectArticle(event);
            listener.handleCollectJobCompleted(
                new NewsCollectJobCompletedEvent("naverNewsCollectJob"));
            listener.handleCollectJobCompleted(
                new NewsCollectJobCompletedEvent("hankyungNewsCollectJob"));

            // Then
            then(subscriptionRepository).should().findAllByInterestIdFetchUser(interest.getId());
            then(notificationService).should()
                .createNewArticleNotification(any(User.class), any(Interest.class),
                    any(Integer.class));

        }

        @Test
        void 관심_기사_등록_이벤트_발생_후_알림_전송에_실패_시_예외가_발생한다() {
            // Given
            Interest interest = InterestFixture.createInterestWithId();
            User user = UserFixture.createUser();
            List<ArticleDto> articles = ArticleFixture.createArticleDtoList();
            List<Subscription> subscription = List.of(
                SubscriptionFixture.createSubscription(user, interest));
            NewArticleCollectEvent event = new NewArticleCollectEvent(
                interest.getId(), interest.getName(), articles);

            given(subscriptionRepository.findAllByInterestIdFetchUser(interest.getId())).willReturn(
                subscription);

            doThrow(new RuntimeException("강제 실패")).when(notificationService)
                .createNewArticleNotification(any(), any(), anyInt());

            // When & Then
            listener.handleCollectArticle(event);
            assertThatThrownBy(() -> {
                listener.handleCollectJobCompleted(
                    new NewsCollectJobCompletedEvent("naverNewsCollectJob"));
                listener.handleCollectJobCompleted(
                    new NewsCollectJobCompletedEvent("hankyungNewsCollectJob"));
            }).isInstanceOf(NotificationSendException.class);

            then(subscriptionRepository).should().findAllByInterestIdFetchUser(interest.getId());
        }
    }
}
