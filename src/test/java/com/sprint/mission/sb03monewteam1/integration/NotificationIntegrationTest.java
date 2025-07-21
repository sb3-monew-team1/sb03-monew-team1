package com.sprint.mission.sb03monewteam1.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.NewArticleCollectEvent;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.event.NewsCollectJobCompletedEvent;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.fixture.NotificationFixture;
import com.sprint.mission.sb03monewteam1.fixture.SubscriptionFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.jpa.article.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.comment.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.interest.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.notification.NotificationRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.subscription.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.user.UserRepository;
import com.sprint.mission.sb03monewteam1.service.CommentServiceImpl;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@LoadTestEnv
@SpringBootTest
@AutoConfigureMockMvc
@EnableAsync
@ActiveProfiles("test")
@DisplayName("NotificationIntegration 테스트")
public class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setup() {
        notificationRepository.deleteAll();
        subscriptionRepository.deleteAll();
        commentRepository.deleteAll();
        articleRepository.deleteAll();
        interestRepository.deleteAll();
        userRepository.deleteAll();

    }

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
                    assertThat(notification.getResourceType()).isEqualTo(ResourceType.COMMENT);
                    assertThat(notification.getResourceId()).isEqualTo(comment.getId());
                    assertThat(notification.getContent()).contains(liker.getNickname());
                });
        }
    }

    @Nested
    @DisplayName("관심 기사 알림 등록 테스트")
    class InterestNotifyTest {

        @Test
        void 관심_기사_등록_이벤트가_발행되면_레포지토리에_알림이_저장되어야_한다() {
            // Given
            User user1 = UserFixture.createUser(
                "user1@exapmle.com",
                "user1",
                "!Password123"
            );
            User savedUser1 = userRepository.save(user1);

            User user2 = UserFixture.createUser(
                "user2@exapmle.com",
                "user2",
                "!Password123"
            );
            User savedUser2 = userRepository.save(user2);

            User user3 = UserFixture.createUser(
                "user3@exapmle.com",
                "user3",
                "!Password123"
            );
            User savedUser3 = userRepository.save(user3);

            Interest interest = InterestFixture.createInterest();
            Interest savedInterest = interestRepository.save(interest);

            Subscription subscription1 = SubscriptionFixture.createSubscription(savedUser1,
                savedInterest);
            subscriptionRepository.save(subscription1);

            Subscription subscription2 = SubscriptionFixture.createSubscription(savedUser2,
                savedInterest);
            subscriptionRepository.save(subscription2);

            Subscription subscription3 = SubscriptionFixture.createSubscription(savedUser3,
                savedInterest);
            subscriptionRepository.save(subscription3);

            List<ArticleDto> articles = ArticleFixture.createArticleDtoList();

            // When
            eventPublisher.publishEvent(new NewArticleCollectEvent(
                savedInterest.getId(), savedInterest.getName(), articles));
            eventPublisher.publishEvent(new NewsCollectJobCompletedEvent("naverNewsCollectJob"));
            eventPublisher.publishEvent(new NewsCollectJobCompletedEvent("hankyungNewsCollectJob"));

            // Then
            Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<Notification> notifications = notificationRepository.findAll();
                    assertThat(notifications.size()).isEqualTo(3);
                    assertThat(notifications.get(0).getResourceType()).isEqualTo(
                        ResourceType.INTERREST);
                    assertThat(notifications.get(0).getUser().getId()).isEqualTo(user1.getId());
                });

        }

    }

    @Nested
    @DisplayName("알림 목록 조회 테스트")
    class NotificationListTest {

        @Test
        void 미확인_알림_목록_조회_시_200이_반환되어야_한다() throws Exception {
            // Given
            User user = UserFixture.createUser();
            User savedUser = userRepository.save(user);
            UUID userId = savedUser.getId();

            List<Notification> notifications = NotificationFixture.createUncheckedNotifications(
                savedUser, 5);
            notificationRepository.saveAll(notifications);

            // When & Then
            mockMvc.perform(get("/api/notifications")
                    .header("Monew-Request-User-ID", userId)
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.size").value(5));
        }

        @Test
        void 다른_사용자의_알림_목록_조회_시_빈_목록이_반환되어야_한다() throws Exception {
            // Given
            User user1 = UserFixture.createUser();
            User savedUser1 = userRepository.save(user1);

            User user2 = UserFixture.createUser("other@example.com", "otherUser", "Password123!");
            User savedUser2 = userRepository.save(user2);

            List<Notification> notifications = NotificationFixture.createUncheckedNotifications(
                savedUser1, 3);
            notificationRepository.saveAll(notifications);

            // When & Then
            mockMvc.perform(get("/api/notifications")
                    .header("Monew-Request-User-ID", savedUser2.getId())
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.size").value(0));
        }

        @Test
        void 알림_목록_조회_시_limit_파라미터가_정상적으로_적용되어야_한다() throws Exception {
            // Given
            User user = UserFixture.createUser();
            User savedUser = userRepository.save(user);
            UUID userId = savedUser.getId();

            List<Notification> notifications = NotificationFixture.createUncheckedNotifications(
                savedUser, 15);
            notificationRepository.saveAll(notifications);

            // When & Then
            mockMvc.perform(get("/api/notifications")
                    .header("Monew-Request-User-ID", userId)
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.size").value(10));

        }
    }

    @Nested
    @DisplayName("알림 수정 테스트")
    class NotificationUpdateTests {

        @Test
        @Transactional
        void 알림을_확인하면_204와_확인여부가_true로_수정되어야_한다() throws Exception {

            // given
            User user = userRepository.save(
                User.builder()
                    .email("author@codeit.com")
                    .nickname("author")
                    .password("author1234!")
                    .build()
            );
            Notification notification = notificationRepository.save(
                NotificationFixture.createNewArticleNotification(user)
            );

            // when & then
            mockMvc.perform(patch("/api/notifications/" + notification.getId())
                    .header("Monew-Request-User-ID", user.getId().toString()))
                .andExpect(status().isNoContent());

            assertThat(notification.isChecked()).isTrue();
        }

        @Test
        void 알림을_전체_확인하면_204와_확인여부가_true로_수정되어야_한다() throws Exception {

            // given
            User user = userRepository.save(UserFixture.createUser());
            List<Notification> notifications = notificationRepository.saveAll(NotificationFixture.createUncheckedNotifications(user, 5));

            // when & then
            mockMvc.perform(patch("/api/notifications")
                    .header("Monew-Request-User-ID", user.getId().toString()))
                .andExpect(status().isNoContent());

            List<Notification> updatedNotifications = notificationRepository.findAllById(
                notifications.stream().map(Notification::getId).toList()
            );

            assertThat(updatedNotifications)
                .hasSize(5)
                .allMatch(Notification::isChecked);
        }

        @Test
        void 존재하지_않는_유저가_알림을_전체_확인하면_404가_반환되어야_한다() throws Exception {

            // given
            UUID invalidUserId = UUID.randomUUID();

            // when & then
            mockMvc.perform(patch("/api/notifications")
                .header("Monew-Request-User-ID", invalidUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }
    }
}
