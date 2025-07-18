package com.sprint.mission.sb03monewteam1.event;

import static org.mockito.Mockito.verify;

import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.listener.NotificationEventListener;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.service.NotificationService;
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
            Comment comment = CommentFixture.createComment("좋아요 이벤트 리스너 테스트", user, ArticleFixture.createArticle());
            CommentLikeEvent event = new CommentLikeEvent(user, comment);

            // when
            listener.handleCommentLike(event);

            // then
            verify(notificationService).createCommentLikeNotification(user, comment);
        }
    }
}
