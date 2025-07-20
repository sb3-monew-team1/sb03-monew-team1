package com.sprint.mission.sb03monewteam1.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.document.*;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewActivityDto;
import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import com.sprint.mission.sb03monewteam1.dto.CommentLikeActivityDto;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.repository.jpa.user.UserRepository;
import com.sprint.mission.sb03monewteam1.repository.mongodb.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@LoadTestEnv
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("UserActivity 통합 테스트")
class UserActivityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionActivityRepository subscriptionActivityRepository;

    @Autowired
    private CommentActivityRepository commentActivityRepository;

    @Autowired
    private CommentLikeActivityRepository commentLikeActivityRepository;

    @Autowired
    private ArticleViewActivityRepository articleViewActivityRepository;

    @Nested
    @DisplayName("UserActivityIntegration 테스트")
    class UserActivityReadTest {

        @Test
        void 사용자_활동_목록을_조회하면_200을_반환한다() throws Exception {
            // Given
            User user = userRepository.save(User.builder()
                .email("test@example.com")
                .nickname("tester")
                .password("pw123!@#")
                .build());

            UUID userId = user.getId();

            subscriptionActivityRepository.save(SubscriptionActivity.builder()
                .userId(userId)
                .subscriptions(List.of())
                .build());

            commentActivityRepository.save(CommentActivity.builder()
                .userId(userId)
                .comments(List.of(CommentActivityDto.builder()
                    .content("sample comment")
                    .createdAt(Instant.now())
                    .articleId(UUID.randomUUID())
                    .userId(userId)
                    .userNickname("tester")
                    .build()))
                .build());

            commentLikeActivityRepository.save(CommentLikeActivity.builder()
                .userId(userId)
                .commentLikes(List.of(CommentLikeActivityDto.builder()
                    .commentId(UUID.randomUUID())
                    .commentContent("nice!")
                    .createdAt(Instant.now())
                    .articleId(UUID.randomUUID())
                    .commentUserId(UUID.randomUUID())
                    .commentUserNickname("someone")
                    .build()))
                .build());

            articleViewActivityRepository.save(ArticleViewActivity.builder()
                .userId(userId)
                .articleViews(List.of(ArticleViewActivityDto.builder()
                    .articleId(UUID.randomUUID())
                    .articleTitle("some title")
                    .createdAt(Instant.now())
                    .viewedBy(userId)
                    .build()))
                .build());

            // When & Then
            mockMvc.perform(get("/api/user-activities/{userId}", userId)
                    .header("Monew-Request-User-ID", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("tester"))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.commentLikes").isArray())
                .andExpect(jsonPath("$.articleViews").isArray());
        }

        @Test
        void 존재하지_않는_사용자면_404를_반환한다() throws Exception {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(get("/api/user-activities/{userId}", nonExistentUserId)
                    .header("Monew-Request-User-ID", nonExistentUserId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
        }
    }
}
