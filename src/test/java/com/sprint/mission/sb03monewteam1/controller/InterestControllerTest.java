package com.sprint.mission.sb03monewteam1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponse;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.service.InterestService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterestController.class)
@ActiveProfiles("test")
@DisplayName("InterestController 테스트")
class InterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterestService interestService;

    @Test
    void 관심사를_등록하면_201과_DTO가_반환된다() throws Exception {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
            "축구", List.of("스포츠")
        );

        InterestResponse response = new InterestResponse(
            UUID.randomUUID(),
            "축구",
            List.of("스포츠"),
            0L,
            false
        );

        given(interestService.create(any())).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("축구"));
    }

    @Test
    void 관심사_등록시_관심사가_중복되면_409를_반환한다() throws Exception {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
            "축구", List.of("스포츠")
        );

        given(interestService.create(any()))
            .willThrow(new InterestDuplicateException("이미 존재하는 관심사입니다."));

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void 관심사_등록시_이름이_비어있으면_400을_반환한다() throws Exception {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
            "", List.of("스포츠")
        );

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.name").value(containsString("관심사 이름은 필수입니다.")));
    }

    @Test
    void 관심사_등록시_키워드가_없으면_400을_반환한다() throws Exception {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
            "축구", List.of()
        );

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.keywords").value(
                containsString("키워드는 최소 1개 이상")
            ));
    }

    @Test
    void 관심사_이름_유사도가_80_퍼센트_이상일_경우_409_Conflict를_반환한다() throws Exception {
        // Given
        InterestRegisterRequest similarRequest = InterestFixture.createRequestWithSimilarName();

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(similarRequest)))
            .andExpect(status().isConflict())  // Expected status: 409 Conflict
            .andExpect(jsonPath("$.code").value("INTEREST_SIMILARITY_ERROR"))
            .andExpect(jsonPath("$.message").value("유사한 관심사 이름이 존재합니다."));
    }
}
