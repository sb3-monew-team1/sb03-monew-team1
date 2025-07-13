package com.sprint.mission.sb03monewteam1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestSimilarityException;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import com.sprint.mission.sb03monewteam1.service.InterestService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockitoBean
    private InterestRepository interestRepository;

    @Nested
    @DisplayName("관심사 생성 태스트")
    class InterestCreateTests {

        @Test
        void 관심사를_등록하면_201과_DTO가_반환된다() throws Exception {
            // Given
            InterestRegisterRequest request = new InterestRegisterRequest(
                "축구", List.of("스포츠")
            );

            InterestDto response = new InterestDto(
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
        void 관심사_이름_유사도가_80퍼센트_이상일_경우_409를_반환한다() throws Exception {
            // Given
            InterestRegisterRequest request = InterestFixture.createInterestRegisterRequestWithSimilarName();

            given(interestService.create(any()))
                .willThrow(new InterestSimilarityException("유사한 관심사 이름이 존재합니다."));

            // When & Then
            mockMvc.perform(post("/api/interests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTEREST_SIMILARITY_ERROR"))
                .andExpect(jsonPath("$.message").value("유사한 관심사 이름이 존재합니다."));
        }
    }

    @Nested
    @DisplayName("관심사 조회 테스트")
    class InterestReadTests {

        private static final UUID REQUEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        @Test
        void 관심사_목록을_조회하면_관심사_목록을_반환한다() throws Exception {
            // Given
            List<InterestDto> interestDtos = InterestFixture.createInterestDtoList();

            given(interestService.search(
                REQUEST_USER_ID, null, null, 10, "name", "asc"))
                .willReturn(interestDtos);

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].name").value("aesthetic"))
                .andExpect(jsonPath("$.content[1].name").value("beauty"))
                .andExpect(jsonPath("$.content[2].name").value("football"))
                .andExpect(jsonPath("$.content[3].name").value("soccer"));
        }

        @Test
        void 관심사를_이름으로_검색하면_부분일치하는_관심사만_조회된다() throws Exception {
            // Given
            List<InterestDto> interestDtos = List.of(
                InterestFixture.createInterestResponseDto("football", List.of("club", "sport")),
                InterestFixture.createInterestResponseDto("soccer", List.of("ball", "sports"))
            );

            given(interestService.search(
                REQUEST_USER_ID, "football", null, 10, "name", "asc"))
                .willReturn(interestDtos);

            // When & Then
            mockMvc.perform(get("/api/interests?keyword=football")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("football"))
                .andExpect(jsonPath("$.content[1].name").value("soccer"));
        }

        @Test
        void 관심사를_구독자수로_정렬하여_반환한다() throws Exception {
            // Given
            List<InterestDto> interestDtos = InterestFixture.createInterestDtoList();

            given(interestService.search(
                REQUEST_USER_ID, null, null, 10, "subscriberCount", "desc"))
                .willReturn(interestDtos);

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("orderBy", "subscriberCount")
                    .param("sortDirection", "desc")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("football"))
                .andExpect(jsonPath("$.content[1].name").value("soccer"))
                .andExpect(jsonPath("$.content[2].name").value("aesthetic"))
                .andExpect(jsonPath("$.content[3].name").value("beauty"))
                .andExpect(jsonPath("$.content[0].subscriberCount").value(200L))
                .andExpect(jsonPath("$.content[1].subscriberCount").value(150L))
                .andExpect(jsonPath("$.content[2].subscriberCount").value(100L))
                .andExpect(jsonPath("$.content[3].subscriberCount").value(50L));
        }

        @Test
        void 관심사를_이름순으로_정렬하여_반환한다() throws Exception {
            // Given
            List<InterestDto> interestDtos = InterestFixture.createInterestDtoList();

            given(interestService.search(
                REQUEST_USER_ID, null, null, 10, "name", "asc"))
                .willReturn(interestDtos);

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("orderBy", "name")
                    .param("sortDirection", "asc")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("aesthetic"))
                .andExpect(jsonPath("$.content[1].name").value("beauty"))
                .andExpect(jsonPath("$.content[2].name").value("football"))
                .andExpect(jsonPath("$.content[3].name").value("soccer"));
        }
    }
}
