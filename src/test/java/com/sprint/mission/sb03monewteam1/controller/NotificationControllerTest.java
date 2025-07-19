package com.sprint.mission.sb03monewteam1.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.notification.NotificationNotFoundException;
import com.sprint.mission.sb03monewteam1.fixture.NotificationFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.service.NotificationService;
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
    @DisplayName("알림 수정 테스트")
    class NotificationUpdateTests {

        @Test
        void 알림을_확인하면_200과_확인여부가_수정된_DTO가_반환되어야_한다() throws Exception {

            // given
            UUID userId = UUID.randomUUID();
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);

            UUID notificationId = UUID.randomUUID();
            Notification notification = NotificationFixture.createNewArticleNotification(user);
            ReflectionTestUtils.setField(notification, "id", notificationId);

            NotificationDto expectedDto = NotificationFixture.createNotificationDtoWithConfirmed(notification, true);

            given(notificationService.confirm(notificationId)).willReturn(expectedDto);

            // When & Then
            mockMvc.perform(patch("/api/notifications/" + notificationId)
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.confirmed").value(true));
        }

        @Test
        void 존재하지_않는_알림을_확인하면_404가_반환되어야_한다() throws Exception {

            // given
            UUID invalidNotificationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(notificationService.confirm(invalidNotificationId))
                .willThrow(new NotificationNotFoundException(invalidNotificationId));

            // when & then
            mockMvc.perform(patch("/api/notifications/" + invalidNotificationId)
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOTIFICATION_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }
    }
}
