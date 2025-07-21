package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.fixture.NotificationFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.NotificationMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.notification.NotificationRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.user.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
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
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    @DisplayName("테스트 환경 설정 확인")
    public void setup() {
        assertNotNull(notificationRepository);
        assertNotNull(userRepository);
        assertNotNull(notificationService);
        assertNotNull(notificationMapper);
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
            assertThat(saved.getResourceType()).isEqualTo(ResourceType.COMMENT);
            assertThat(saved.getResourceId()).isEqualTo(commentId);
            assertThat(saved.getContent()).isEqualTo(expectedContent);
        }
    }

    @Nested
    @DisplayName("미확인 알림 목록 테스트")
    class UnreadNotificationListTest {
        @Test
        void 미확인_알림_목록을_조회하면_CursorPageResponse를_반환한다() {
            // Given
            UUID userId = UserFixture.getDefaultId();
            String cursor = "2024-01-01T12:00:00Z";
            Instant after = Instant.parse("2024-01-01T12:00:00Z");
            int limit = 10;

            User user = UserFixture.createUser();
            List<Notification> notifications = NotificationFixture.createUncheckedNotifications(user, 5);
            List<NotificationDto> notificationDtos = List.of(
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 1", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 2", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 3", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 4", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 5", false)
            );

            given(notificationRepository.findUncheckedNotificationsWithCursor(
                eq(userId), eq(cursor), eq(after), eq(limit + 1))).willReturn(notifications);
            given(notificationMapper.toDto(any(Notification.class)))
                .willReturn(notificationDtos.get(0), notificationDtos.get(1),
                    notificationDtos.get(2), notificationDtos.get(3), notificationDtos.get(4));
            given(notificationRepository.countByUserIdAndIsCheckedFalse(eq(userId)))
                .willReturn(5L);

            // When
            CursorPageResponse<NotificationDto> result = notificationService.getUncheckedNotifications(
                userId, cursor, after, limit);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(5);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.size()).isEqualTo(5);
            assertThat(result.totalElements()).isEqualTo(5L);

            then(notificationRepository).should().findUncheckedNotificationsWithCursor(
                eq(userId), eq(cursor), eq(after), eq(limit + 1));
            then(notificationMapper).should(times(5)).toDto(any(Notification.class));
        }

        @Test
        void 한_페이지보다_많은_알림이_있으면_hasNext가_true이다() {
            // Given
            UUID userId = UserFixture.getDefaultId();
            String cursor = "2024-01-01T12:00:00Z";
            Instant after = Instant.parse("2024-01-01T12:00:00Z");
            int limit = 5;

            User user = UserFixture.createUser();
            List<Notification> notifications = NotificationFixture.createUncheckedNotifications(user, 6); // limit + 1

            notifications.forEach(n -> ReflectionTestUtils.setField(n, "createdAt", Instant.now()));

            List<NotificationDto> notificationDtos = List.of(
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 1", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 2", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 3", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 4", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 5", false)
            );

            given(notificationRepository.findUncheckedNotificationsWithCursor(
                eq(userId), eq(cursor), eq(after), eq(limit + 1))).willReturn(notifications);
            given(notificationMapper.toDto(any(Notification.class)))
                .willReturn(notificationDtos.get(0), notificationDtos.get(1),
                    notificationDtos.get(2), notificationDtos.get(3), notificationDtos.get(4));

            // When
            CursorPageResponse<NotificationDto> result = notificationService.getUncheckedNotifications(
                userId, cursor, after, limit);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(5);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.size()).isEqualTo(5);
            assertThat(result.nextCursor()).isNotNull();
            assertThat(result.nextAfter()).isNotNull();

            then(notificationRepository).should().findUncheckedNotificationsWithCursor(
                eq(userId), eq(cursor), eq(after), eq(limit + 1));
            then(notificationMapper).should(times(5)).toDto(any(Notification.class));
        }

        @Test
        void 커서가_null이면_첫_페이지를_조회한다() {
            // Given
            UUID userId = UserFixture.getDefaultId();
            String cursor = null;
            Instant after = null;
            int limit = 10;

            User user = UserFixture.createUser();
            List<Notification> notifications = NotificationFixture.createUncheckedNotifications(user, 3);
            List<NotificationDto> notificationDtos = List.of(
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 1", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 2", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 3", false)
            );

            given(notificationRepository.findUncheckedNotificationsWithCursor(
                eq(userId), eq(cursor), eq(after), eq(limit + 1))).willReturn(notifications);
            given(notificationMapper.toDto(any(Notification.class)))
                .willReturn(notificationDtos.get(0), notificationDtos.get(1), notificationDtos.get(2));

            // When
            CursorPageResponse<NotificationDto> result = notificationService.getUncheckedNotifications(
                userId, cursor, after, limit);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(3);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextAfter()).isNull();

            then(notificationRepository).should().findUncheckedNotificationsWithCursor(
                eq(userId), eq(cursor), eq(after), eq(limit + 1));
            then(notificationMapper).should(times(3)).toDto(any(Notification.class));
        }

        @Test
        void 알림이_없으면_빈_목록을_반환한다() {
            // Given
            UUID userId = UserFixture.getDefaultId();
            String cursor = "2024-01-01T12:00:00Z";
            Instant after = Instant.parse("2024-01-01T12:00:00Z");
            int limit = 10;

            List<Notification> notifications = List.of();

            given(notificationRepository.findUncheckedNotificationsWithCursor(
                eq(userId), eq(cursor), eq(after), eq(limit + 1))).willReturn(notifications);

            // When
            CursorPageResponse<NotificationDto> result = notificationService.getUncheckedNotifications(
                userId, cursor, after, limit);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.size()).isZero();
            assertThat(result.totalElements()).isZero();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextAfter()).isNull();

            then(notificationRepository).should().findUncheckedNotificationsWithCursor(
                eq(userId), eq(cursor), eq(after), eq(limit + 1));
            then(notificationMapper).shouldHaveNoInteractions();
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

            given(notificationRepository.findById(notificationId)).willReturn(
                Optional.of(notification));

            // when
            notificationService.confirm(notificationId, userId);

            // then
            assertThat(notification.isChecked()).isTrue();
        }

        @Test
        void 존재하지_않는_알림을_확인하면_예외가_발생한다() {

            // given
            UUID invalidNotificationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(notificationRepository.findById(invalidNotificationId)).willReturn(
                Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
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

            given(notificationRepository.findById(notificationId)).willReturn(
                Optional.of(notification));

            // when & then
            assertThatThrownBy(() -> {
                notificationService.confirm(notificationId, invalidUserId);
            }).isInstanceOf(NotificationAccessDeniedException.class);

            then(notificationRepository).should().findById(notificationId);
        }

        @Test
        void 알림을_전체_확인하면_모든_알림의_확인여부가_수정되어야_한다() {

            // given
            UUID userId = UUID.randomUUID();
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);

            List<Notification> notifications = new ArrayList<>();
            List<NotificationDto> notificationDtos = new ArrayList<>();

            for (int i=0; i<5; i++) {
                UUID notificationId = UUID.randomUUID();
                Notification notification = NotificationFixture.createNewArticleNotification(user);
                ReflectionTestUtils.setField(notification, "id", notificationId);
                notifications.add(notification);

                NotificationDto expectedDto = NotificationFixture.createNotificationDtoWithConfirmed(notification, true);
                notificationDtos.add(expectedDto);
            }

            given(userRepository.existsByIdAndIsDeletedFalse(userId)).willReturn(true);
            given(notificationRepository.markAllAsCheckedByUserId(userId)).willReturn(5);

            // when
            notificationService.confirmAll(userId);

            // then
            then(notificationRepository).should().markAllAsCheckedByUserId(userId);
        }

        @Test
        void 확인되지_않은_알림이_없는_경우_빈_리스트를_반환한다() {

            // given
            UUID userId = UUID.randomUUID();

            given(userRepository.existsByIdAndIsDeletedFalse(userId)).willReturn(true);
            given(notificationRepository.markAllAsCheckedByUserId(userId)).willReturn(0);

            // when & then
            assertThatCode(() -> notificationService.confirmAll(userId))
                .doesNotThrowAnyException();
        }

        @Test
        void 존재하지_않는_유저일_경우_예외를_던진다() {

            // given
            UUID invalidUserId = UUID.randomUUID();

            given(userRepository.existsByIdAndIsDeletedFalse(invalidUserId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> notificationService.confirmAll(invalidUserId))
                .isInstanceOf(UserNotFoundException.class);
        }
    }
}
