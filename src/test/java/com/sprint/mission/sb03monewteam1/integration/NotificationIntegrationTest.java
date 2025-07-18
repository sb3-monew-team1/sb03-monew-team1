package com.sprint.mission.sb03monewteam1.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.repository.jpa.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.NotificationRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;
import com.sprint.mission.sb03monewteam1.service.CommentServiceImpl;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;

@LoadTestEnv
@SpringBootTest
@AutoConfigureMockMvc
@EnableAsync
@ActiveProfiles("test")
@DisplayName("InterestIntegration 테스트")
public class NotificationIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentServiceImpl commentService;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("좋아요 알림 등록 테스트")
    class NotifyCommentLikeTest {

        @Test
        void 좋아요를_등록하면_알림이_Repository에_저장된다() throws InterruptedException {

            // given
            Article article = articleRepository.save(
                Article.builder()
                    .source("NAVER")
                    .sourceUrl("https://news.naver.com/article/test")
                    .title("좋아요 등록 테스트 기사")
                    .summary("테스트 요약")
                    .publishDate(Instant.now())
                    .build()
            );
            User author = userRepository.save(
                User.builder()
                    .email("author@codeit.com")
                    .nickname("author")
                    .password("author1234!")
                    .build()
            );
            User liker = userRepository.save(
                User.builder()
                    .email("liker@codeit.com")
                    .nickname("liker")
                    .password("liker1234!")
                    .build()
            );
            Comment comment = commentRepository.save(
                Comment.builder()
                    .content("좋아요 등록 테스트")
                    .author(author)
                    .article(article)
                    .likeCount(0L)
                    .build()
            );

            // when
            commentService.like(comment.getId(), liker.getId());

            // then
            await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Notification notification = notificationRepository.findAll().get(0);
                    assertThat(notification.getUser().getId()).isEqualTo(author.getId());
                    assertThat(notification.getResourceType()).isEqualTo(ResourceType.comment);
                    assertThat(notification.getResourceId()).isEqualTo(comment.getId());
                    assertThat(notification.getContent()).contains(liker.getNickname());
                });
        }
    }
}
