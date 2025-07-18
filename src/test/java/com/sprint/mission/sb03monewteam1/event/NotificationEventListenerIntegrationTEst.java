package com.sprint.mission.sb03monewteam1.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.fixture.SubscriptionFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.jpa.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.NotificationRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;
import java.time.Duration;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@LoadTestEnv
@ActiveProfiles("test")
@DisplayName("NotificationIntegration 테스트")
public class NotificationEventListenerIntegrationTEst {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;


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

        Subscription subscription1 = SubscriptionFixture.createSubscription(savedUser1, savedInterest);
        subscriptionRepository.save(subscription1);

        Subscription subscription2 = SubscriptionFixture.createSubscription(savedUser2, savedInterest);
        subscriptionRepository.save(subscription2);

        Subscription subscription3 = SubscriptionFixture.createSubscription(savedUser3, savedInterest);
        subscriptionRepository.save(subscription3);

        List<ArticleDto> articles = ArticleFixture.createArticleDtoList();

        // When
        eventPublisher.publishEvent(new NewArticleCollectEvent(savedInterest, articles));

        // Then
        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<Notification> notifications = notificationRepository.findAll();
                assertThat(notifications.size()).isEqualTo(3);
                assertThat(notifications.get(0).getResourceType()).isEqualTo(ResourceType.interest);
                assertThat(notifications.get(0).getUser().getId()).isEqualTo(user1.getId());
            });

    }

}
