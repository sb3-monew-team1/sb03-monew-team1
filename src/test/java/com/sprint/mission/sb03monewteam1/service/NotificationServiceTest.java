package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.jpa.NotificationRepository;
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

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {

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
}
