package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.sprint.mission.sb03monewteam1.document.ArticleViewActivity;
import com.sprint.mission.sb03monewteam1.document.CommentActivity;
import com.sprint.mission.sb03monewteam1.document.CommentLikeActivity;
import com.sprint.mission.sb03monewteam1.document.SubscriptionActivity;
import com.sprint.mission.sb03monewteam1.dto.*;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.user.UserException;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;
import com.sprint.mission.sb03monewteam1.repository.mongodb.ArticleViewActivityRepository;
import com.sprint.mission.sb03monewteam1.repository.mongodb.CommentActivityRepository;
import com.sprint.mission.sb03monewteam1.repository.mongodb.CommentLikeActivityRepository;
import com.sprint.mission.sb03monewteam1.repository.mongodb.SubscriptionActivityRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserActivityService 테스트")
class UserActivityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionActivityRepository subscriptionActivityRepository;

    @Mock
    private CommentActivityRepository commentActivityRepository;

    @Mock
    private CommentLikeActivityRepository commentLikeActivityRepository;

    @Mock
    private ArticleViewActivityRepository articleViewActivityRepository;

    @InjectMocks
    private UserActivityServiceImpl userActivityService;

    @Nested
    @DisplayName("사용자 활동 내역 조회 테스트")
    class GetUserActivityTest {

        @Test
        void 사용자_활동_내역을_조회하면_활동_내역_DTO를_반환한다() {
            // Given
            User user = UserFixture.createUser();
            UUID userId = user.getId();

            SubscriptionActivity subscriptionActivity = SubscriptionActivity.builder()
                .userId(userId)
                .subscriptions(List.of(SubscriptionDto.builder().interestId(UUID.randomUUID()).build()))
                .build();

            CommentActivity commentActivity = CommentActivity.builder()
                .userId(userId)
                .comments(List.of(CommentActivityDto.builder().content("test comment").build()))
                .build();

            CommentLikeActivity likeActivity = CommentLikeActivity.builder()
                .userId(userId)
                .commentLikes(List.of(CommentLikeActivityDto.builder().commentId(UUID.randomUUID()).build()))
                .build();

            ArticleViewActivity viewActivity = ArticleViewActivity.builder()
                .userId(userId)
                .articleViews(List.of(ArticleViewActivityDto.builder().articleId(UUID.randomUUID()).build()))
                .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(subscriptionActivityRepository.findById(userId)).willReturn(Optional.of(subscriptionActivity));
            given(commentActivityRepository.findRecent10CommentsByUserId(userId)).willReturn(Optional.of(commentActivity));
            given(commentLikeActivityRepository.findRecent10CommentLikesByUserId(userId)).willReturn(Optional.of(likeActivity));
            given(articleViewActivityRepository.findRecent10ArticleViewsByUserId(userId)).willReturn(Optional.of(viewActivity));

            // When
            UserActivityDto result = userActivityService.getUserActivity(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.comments()).hasSize(1);
            assertThat(result.commentLikes()).hasSize(1);
            assertThat(result.articleViews()).hasSize(1);
            assertThat(result.subscriptions()).hasSize(1);
        }

        @Test
        void 사용자가_존재하지_않을_경우_예외가_발생한다() {
            // Given
            UUID userId = UUID.randomUUID();
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When
            Throwable thrown = catchThrowable(() -> userActivityService.getUserActivity(userId));

            // Then
            assertThat(thrown)
                .isInstanceOf(UserException.class) // 수정된 예외 클래스 검증
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
        }

        @Test
        void 리포지토리에_값이_없는_경우_빈_리스트를_반환한다() {
            // Given
            User user = UserFixture.createUser();
            UUID userId = user.getId();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(subscriptionActivityRepository.findById(userId)).willReturn(Optional.empty());
            given(commentActivityRepository.findRecent10CommentsByUserId(userId)).willReturn(Optional.empty());
            given(commentLikeActivityRepository.findRecent10CommentLikesByUserId(userId)).willReturn(Optional.empty());
            given(articleViewActivityRepository.findRecent10ArticleViewsByUserId(userId)).willReturn(Optional.empty());

            // When
            UserActivityDto result = userActivityService.getUserActivity(userId);

            // Then
            assertThat(result.subscriptions()).isEmpty();
            assertThat(result.comments()).isEmpty();
            assertThat(result.commentLikes()).isEmpty();
            assertThat(result.articleViews()).isEmpty();
        }
    }
}
