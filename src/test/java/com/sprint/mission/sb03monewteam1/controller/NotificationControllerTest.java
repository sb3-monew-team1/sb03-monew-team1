package com.sprint.mission.sb03monewteam1.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.fixture.NotificationFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.service.NotificationService;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(NotificationController.class)
@DisplayName("Notification 테스트")
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    @DisplayName("테스트 환경 설정 확인")
    void setup() {
        assertNotNull(mockMvc);
        assertNotNull(objectMapper);
        assertNotNull(notificationService);
    }

    @Nested
    @DisplayName("알림 목록 조회 테스트")
    class NotificationListTests {

        @Test
        void Size보다_적은_알림_목록을_조회하면_200이_반환된다() throws Exception {
            // Given
            UUID userId = UserFixture.getDefaultId();
            List<NotificationDto> notifications = Arrays.asList(
                NotificationFixture.createNotificationDto(randomUUID(), "알림 1", false),
                NotificationFixture.createNotificationDto(randomUUID(), "알림 2", false),
                NotificationFixture.createNotificationDto(randomUUID(), "알림 3", false)
            );

            CursorPageResponse<NotificationDto> response = CursorPageResponse.<NotificationDto>builder()
                .content(notifications)
                .nextCursor(null)
                .nextAfter(null)
                .size(3)
                .totalElements(3L)
                .hasNext(false)
                .build();

            given(notificationService.getUncheckedNotifications(
                eq(userId), isNull(), isNull(), eq(50)
            )).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/notifications")
                    .header("Monew-Request-User-ID", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].content").value("알림 1"))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.nextCursor").isEmpty())
                .andExpect(jsonPath("$.nextAfter").isEmpty());
        }

        @Test
        void 커서와_after로_알림_목록을_조회하면_200이_반환된다() throws Exception {
            // Given
            UUID userId = UserFixture.getDefaultId();
            String cursor = "2024-01-01T12:00:00Z";
            Instant after = Instant.parse("2024-01-01T12:00:00Z");
            int limit = 50;

            List<NotificationDto> notifications = Arrays.asList(
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "이후 알림 1", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "이후 알림 2", false)
            );

            CursorPageResponse<NotificationDto> response = CursorPageResponse.<NotificationDto>builder()
                .content(notifications)
                .nextCursor("2024-01-01T11:00:00Z")
                .nextAfter(Instant.parse("2024-01-01T11:00:00Z"))
                .size(2)
                .totalElements(2L)
                .hasNext(true)
                .build();

            given(notificationService.getUncheckedNotifications(
                eq(userId), eq(cursor), eq(after), eq(limit)
            )).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/notifications")
                    .header("Monew-Request-User-ID", userId.toString())
                    .param("cursor", cursor)
                    .param("after", after.toString())
                    .param("limit", String.valueOf(limit))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].content").value("이후 알림 1"))
                .andExpect(jsonPath("$.content[1].content").value("이후 알림 2"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("2024-01-01T11:00:00Z"))
                .andExpect(jsonPath("$.nextAfter").value("2024-01-01T11:00:00Z"))
                .andExpect(jsonPath("$.size").value(2));
        }

        @Test
        void 알림이_없으면_빈_목록이_반환된다() throws Exception {
            // Given
            UUID userId = UserFixture.getDefaultId();

            CursorPageResponse<NotificationDto> response = CursorPageResponse.<NotificationDto>builder()
                .content(Arrays.asList())
                .nextCursor(null)
                .nextAfter(null)
                .size(0)
                .totalElements(0L)
                .hasNext(false)
                .build();

            given(notificationService.getUncheckedNotifications(
                eq(userId), isNull(), isNull(), eq(50)
            )).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/notifications")
                    .header("Monew-Request-User-ID", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void 페이지가_더_있으면_hasNext가_true이다() throws Exception {
            // Given
            UUID userId = UserFixture.getDefaultId();
            int limit = 5;

            List<NotificationDto> notifications = Arrays.asList(
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 1", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 2", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 3", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 4", false),
                NotificationFixture.createNotificationDto(UUID.randomUUID(), "알림 5", false)
            );

            CursorPageResponse<NotificationDto> response = CursorPageResponse.<NotificationDto>builder()
                .content(notifications)
                .nextCursor("2024-01-01T10:00:00Z")
                .nextAfter(Instant.parse("2024-01-01T10:00:00Z"))
                .size(5)
                .totalElements(5L)
                .hasNext(true)
                .build();

            given(notificationService.getUncheckedNotifications(
                eq(userId), isNull(), isNull(), eq(limit)
            )).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/notifications")
                    .header("Monew-Request-User-ID", userId.toString())
                    .param("limit", String.valueOf(limit))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("2024-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.nextAfter").value("2024-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.size").value(5));
        }
    }

    @Nested
    @DisplayName("알림 수정 테스트")
    class NotificationUpdateTests {

        @Test
        void 알림을_확인하면_204가_반환되어야_한다() throws Exception {

            // given
            UUID userId = UUID.randomUUID();
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);

            UUID notificationId = UUID.randomUUID();
            Notification notification = NotificationFixture.createNewArticleNotification(user);
            ReflectionTestUtils.setField(notification, "id", notificationId);

            willDoNothing()
                .given(notificationService)
                .confirm(notificationId, userId);

            // When & Then
            mockMvc.perform(patch("/api/notifications/" + notificationId)
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNoContent());
        }

        @Test
        void 존재하지_않는_알림을_확인하면_404가_반환되어야_한다() throws Exception {

            // given
            UUID invalidNotificationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willThrow(new NotificationNotFoundException(invalidNotificationId))
                .given(notificationService)
                .confirm(invalidNotificationId, userId);

            // when & then
            mockMvc.perform(patch("/api/notifications/" + invalidNotificationId)
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOTIFICATION_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 대상자가_아닌_유저가_알림을_확인하면_403이_반환되어야_한다() throws Exception {

            // given
            UUID userId = UUID.randomUUID();
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);

            UUID notificationId = UUID.randomUUID();
            Notification notification = NotificationFixture.createNewArticleNotification(user);
            ReflectionTestUtils.setField(notification, "id", notificationId);

            UUID invalidUserId = UUID.randomUUID();

            willThrow(new NotificationAccessDeniedException(invalidUserId))
                .given(notificationService)
                .confirm(notificationId, invalidUserId);

            // when & then
            mockMvc.perform(patch("/api/notifications/" + notificationId)
                    .header("Monew-Request-User-ID", invalidUserId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 알림을_전체_확인하면_204가_반환되어야_한다() throws Exception {

            // given
            UUID userId = UUID.randomUUID();

            willDoNothing().given(notificationService).confirmAll(userId);

            // when & Then
            mockMvc.perform(patch("/api/notifications")
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNoContent());
        }

        @Test
        void 존재하지_않는_유저가_알림을_전체_확인하면_404가_반환되어야_한다() throws Exception {

            // given
            UUID invalidUserId = UUID.randomUUID();

            willThrow(new UserNotFoundException(invalidUserId))
                .given(notificationService)
                .confirmAll(invalidUserId);

            // when & then
            mockMvc.perform(patch("/api/notifications")
                    .header("Monew-Request-User-ID", invalidUserId.toString()))
                .andExpect(status().isNotFound());
        }
    }
}
