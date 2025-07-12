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
        // given
        InterestRegisterRequest request = InterestFixture.createInterestCreateRequest();

        // when & then
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
        // given
        InterestRegisterRequest request = InterestFixture.createRequestWithEmptyName();

        // when & then
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
        // given
        InterestRegisterRequest request = InterestFixture.createInterestCreateRequest();

        // when & then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // then
        assertThat(interestRepository.existsByName(request.name())).isTrue();

        // when & then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INTEREST_DUPLICATE"))
            .andExpect(jsonPath("$.message").value("이미 존재하는 관심사입니다."));
    }
}
