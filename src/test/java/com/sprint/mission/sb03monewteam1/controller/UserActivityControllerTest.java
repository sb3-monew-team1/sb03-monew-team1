package com.sprint.mission.sb03monewteam1.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.*;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.service.UserActivityService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(UserActivityController.class)
@ActiveProfiles("test")
@DisplayName("UserActivityController 테스트")
class UserActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserActivityService userActivityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 사용자_활동_내역을_조회하면_200을_반환한다() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UserActivityDto userActivityDto = UserActivityDto.builder()
            .id(userId)
            .email("test@example.com")
            .nickname("tester")
            .createdAt(Instant.now())
            .subscriptions(List.of())
            .comments(List.of())
            .commentLikes(List.of())
            .articleViews(List.of())
            .build();

        given(userActivityService.getUserActivity(userId)).willReturn(userActivityDto);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user-activities/{userId}", userId)
                .header("Monew-Request-User-ID", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    void 사용자가_존재하지_않을_경우_404를_반환한다() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        given(userActivityService.getUserActivity(userId))
            .willThrow(new UserNotFoundException(userId));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user-activities/{userId}", userId)
                .header("Monew-Request-User-ID", userId.toString()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }
}
