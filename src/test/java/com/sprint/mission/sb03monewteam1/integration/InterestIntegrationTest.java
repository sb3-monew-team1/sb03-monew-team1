package com.sprint.mission.sb03monewteam1.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("InterestIntegration 테스트")
class InterestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InterestRepository interestRepository;

    @Test
    void 관심사를_등록하면_DB에_저장된다() throws Exception {
        // Given
        InterestRegisterRequest request = InterestFixture.createInterestCreateRequest();

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(request.name()));

        boolean exists = interestRepository.existsByName(request.name());
        assertThat(exists).isTrue();
    }

    @Test
    void 관심사_등록시_이름이_비어있으면_400을_반환한다() throws Exception {
        // Given
        InterestRegisterRequest request = InterestFixture.createRequestWithEmptyName();

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value(containsString("관심사 이름은 필수입니다.")))
            .andExpect(jsonPath("$.details.name").value(containsString("관심사 이름은 필수입니다.")));
    }

    @Test
    void 관심사_등록시_관심사가_중복되면_409를_반환한다() throws Exception {
        // Given
        InterestRegisterRequest request = InterestFixture.createInterestCreateRequest();

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Then
        assertThat(interestRepository.existsByName(request.name())).isTrue();

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INTEREST_DUPLICATE"))
            .andExpect(jsonPath("$.message").value("이미 존재하는 관심사입니다."));
    }

    @Test
    void 관심사_이름_유사도가_80_퍼센트_이상일_경우_409를_반환한다() throws Exception {
        // Given: DB에 이미 존재하는 관심사
        InterestRegisterRequest request = InterestFixture.createInterestCreateRequest();
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // DB에 관심사가 등록된 것을 확인
        assertThat(interestRepository.existsByName(request.name())).isTrue();

        // Given: 유사한 이름을 가진 관심사 요청
        InterestRegisterRequest similarRequest = InterestFixture.createRequestWithSimilarName();

        // When & Then: 유사한 이름이 있을 경우 409 Conflict 반환
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(similarRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INTEREST_SIMILARITY_ERROR"))
            .andExpect(jsonPath("$.message").value("유사한 관심사 이름이 존재합니다."));
    }

}
