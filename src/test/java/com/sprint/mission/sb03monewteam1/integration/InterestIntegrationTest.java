package com.sprint.mission.sb03monewteam1.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.repository.InterestKeywordRepository;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@LoadTestEnv
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

    @Autowired
    private InterestKeywordRepository interestKeywordRepository;

    @Nested
    @DisplayName("관심사 생성 태스트")
    class InterestCreateTests {

        @Test
        void 관심사를_등록하면_DB에_저장된다() throws Exception {
            // Given
            InterestRegisterRequest request = InterestFixture.createInterestRegisterRequest();

            // When & Then
            mockMvc.perform(post("/api/interests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.keywords").isArray())
                .andExpect(jsonPath("$.keywords.length()").value(request.keywords().size()));

            boolean exists = interestRepository.existsByName(request.name());
            assertThat(exists).isTrue();

            for (String keyword : request.keywords()) {
                boolean keywordExists = interestKeywordRepository.existsByKeywordAndInterestName(
                    keyword, request.name());
                assertThat(keywordExists).isTrue();
            }
        }

        @Test
        void 관심사_등록시_이름이_비어있으면_400을_반환한다() throws Exception {
            // Given
            InterestRegisterRequest request = InterestFixture.createInterestRegisterRequestWithEmptyName();

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
            InterestRegisterRequest request = InterestFixture.createInterestRegisterRequest();

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
            // Given
            InterestRegisterRequest request = InterestFixture.createInterestRegisterRequest();
            mockMvc.perform(post("/api/interests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            assertThat(interestRepository.existsByName(request.name())).isTrue();

            // Given
            InterestRegisterRequest similarRequest = InterestFixture.createInterestRegisterRequestWithSimilarName();

            // When & Then
            mockMvc.perform(post("/api/interests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(similarRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTEREST_SIMILARITY_ERROR"))
                .andExpect(jsonPath("$.message").value("유사한 관심사 이름이 존재합니다."));
        }
    }

    @Nested
    @DisplayName("관심사 조회 테스트")
    class InterestReadTests {

        @BeforeEach
        void setUp() {
            Interest interest1 = Interest.builder()
                .name("football club")
                .subscriberCount(150L)
                .build();

            Interest interest2 = Interest.builder()
                .name("soccer")
                .subscriberCount(200L)
                .build();

            Interest interest3 = Interest.builder()
                .name("aesthetic")
                .subscriberCount(100L)
                .build();

            Interest interest4 = Interest.builder()
                .name("beauty")
                .subscriberCount(50L)
                .build();

            InterestKeyword keyword1 = InterestKeyword.builder()
                .keyword("sports")
                .interest(interest1)
                .build();

            InterestKeyword keyword2 = InterestKeyword.builder()
                .keyword("club")
                .interest(interest1)
                .build();

            InterestKeyword keyword3 = InterestKeyword.builder()
                .keyword("football")
                .interest(interest2)
                .build();

            InterestKeyword keyword4 = InterestKeyword.builder()
                .keyword("ball")
                .interest(interest2)
                .build();

            interestRepository.saveAll(List.of(interest1, interest2, interest3, interest4));
            interestKeywordRepository.saveAll(List.of(keyword1, keyword2, keyword3, keyword4));
        }

        @Test
        void 관심사_목록을_조회하면_관심사_목록을_반환한다() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("searchKeyword", "")
                    .param("cursor", "")
                    .param("limit", "10")
                    .param("orderBy", "name")
                    .param("direction", "asc")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4)); // 2개의 Interest 객체
        }

        @Test
        void 관심사_이름으로_검색하면_부분일치하는_관심사만_조회된다() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("searchKeyword", "soccer")
                    .param("cursor", "")
                    .param("limit", "10")
                    .param("orderBy", "name")
                    .param("direction", "asc")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("soccer"));
        }

        @Test
        void 관심사_이름순으로_정렬하면_이름순으로_정렬된_목록을_반환한다() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("searchKeyword", "")
                    .param("cursor", "")
                    .param("limit", "10")
                    .param("orderBy", "name")
                    .param("direction", "asc")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("aesthetic"))
                .andExpect(jsonPath("$.content[1].name").value("beauty"))
                .andExpect(jsonPath("$.content[2].name").value("football club"))
                .andExpect(jsonPath("$.content[3].name").value("soccer"));
        }

        @Test
        void 관심사_구독자순으로_내림차순으로_정렬하면_구독자순으로_내림차순으로_정렬된_목록을_반환한다() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("searchKeyword", "")
                    .param("cursor", "")
                    .param("limit", "10")
                    .param("orderBy", "subscriberCount")
                    .param("direction", "desc")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("soccer"))
                .andExpect(jsonPath("$.content[1].name").value("football club"))
                .andExpect(jsonPath("$.content[2].name").value("aesthetic"))
                .andExpect(jsonPath("$.content[3].name").value("beauty"));
        }

        @Test
        void 잘못된_정렬_기준_인경우_400을_반환한다() throws Exception {
            // Given
            int limit = 10;
            String searchKeyword = "soccer";
            String orderBy = "invalidSort";
            String direction = "asc";

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("searchKeyword", searchKeyword)
                    .param("cursor", "")
                    .param("limit", String.valueOf(limit))
                    .param("orderBy", orderBy)
                    .param("direction", direction)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ORDER_PARAMETER"))
                .andExpect(jsonPath("$.message").value("잘못된 정렬 기준입니다."));
        }

        @Test
        void 잘못된_페이지네이션_파라미터_인경우_400을_반환한다() throws Exception {
            // Given
            int limit = 0;
            String orderBy = "name";
            String direction = "asc";

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("searchKeyword", "")
                    .param("cursor", "")
                    .param("limit", String.valueOf(limit))
                    .param("orderBy", orderBy)
                    .param("direction", direction)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())  // 400 응답
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION_PARAMETER"))
                .andExpect(jsonPath("$.message").value("데이터 limit값이 0입니다."));
        }

        @Test
        void 잘못된_cursor값_인경우_400을_반환한다() throws Exception {
            // Given
            int limit = 10;
            String searchKeyword = "soccer";
            String orderBy = "name";
            String direction = "asc";
            String cursor = "invalidCursorFormat";

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("searchKeyword", searchKeyword)
                    .param("cursor", cursor)
                    .param("limit", String.valueOf(limit))
                    .param("orderBy", orderBy)
                    .param("direction", direction)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CURSOR_FORMAT"))
                .andExpect(jsonPath("$.message").value("cursor 값이 잘못되었습니다."));
        }
    }
}
