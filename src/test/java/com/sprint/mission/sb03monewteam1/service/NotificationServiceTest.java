package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationNotFoundException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.fixture.NotificationFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.NotificationMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.NotificationRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 테스트")
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

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

    @Nested
    @DisplayName("좋아요 알림 등록 테스트")
    class NotifyCommentLikeTest {

        @Test
        void 댓글_좋아요를_누르면_알림이_등록된다() {

            // given
            UUID commentId = UUID.randomUUID();

            Article article = ArticleFixture.createArticle();
            User user = UserFixture.createUser();
            Comment comment = CommentFixture.createComment("좋아요 알림 테스트", user, article);
            ReflectionTestUtils.setField(comment, "id", commentId);
            String expectedContent = String.format("%s님이 나의 댓글을 좋아합니다.", user.getNickname());

            // when
            notificationService.createCommentLikeNotification(user, comment);

            // then
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());

            Notification saved = captor.getValue();
            assertThat(saved.getUser().getId()).isEqualTo(comment.getAuthor().getId());
            assertThat(saved.getResourceType()).isEqualTo(ResourceType.comment);
            assertThat(saved.getResourceId()).isEqualTo(commentId);
            assertThat(saved.getContent()).isEqualTo(expectedContent);
        }
    }

    @Nested
    @DisplayName("알림 수정 테스트")
    class NotificationUpdateTests {

        @Test
        void 알림을_확인하면_확인여부가_수정되어야_한다() {

            // given
            UUID userId = UUID.randomUUID();
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);

            UUID notificationId = UUID.randomUUID();
            Notification notification = NotificationFixture.createNewArticleNotification(user);
            ReflectionTestUtils.setField(notification, "id", notificationId);

            NotificationDto expectedDto = NotificationFixture.createNotificationDtoWithConfirmed(notification, true);

            given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
            given(notificationMapper.toDto(any(Notification.class))).willReturn(expectedDto);

            // when
            NotificationDto result = notificationService.confirm(notificationId, userId);

            // then
            assertThat(result.confirmed()).isTrue();
        }

        @Test
        void 존재하지_않는_알림을_확인하면_예외가_발생한다() {

            // given
            UUID invalidNotificationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(notificationRepository.findById(invalidNotificationId)).willReturn(Optional.empty());

            // when & then
            Assertions.assertThatThrownBy(() -> {
                notificationService.confirm(invalidNotificationId, userId);
            }).isInstanceOf(NotificationNotFoundException.class);

            then(notificationRepository).should().findById(invalidNotificationId);
        }

        @Test
        void 알림_대상자가_아닌_유저가_알림을_확인하면_예외가_발생한다() {

            // given
            UUID userId = UUID.randomUUID();
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);

            UUID notificationId = UUID.randomUUID();
            Notification notification = NotificationFixture.createNewArticleNotification(user);
            ReflectionTestUtils.setField(notification, "id", notificationId);

            UUID invalidUserId = UUID.randomUUID();

            given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

            // when & then
            Assertions.assertThatThrownBy(() -> {
                notificationService.confirm(notificationId, invalidUserId);
            }).isInstanceOf(NotificationAccessDeniedException.class);

            then(notificationRepository).should().findById(notificationId);
        }
    }
}
