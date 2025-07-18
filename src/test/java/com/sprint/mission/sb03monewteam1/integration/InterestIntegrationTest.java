package com.sprint.mission.sb03monewteam1.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.repository.jpa.InterestKeywordRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;
import java.util.List;
import java.util.UUID;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

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

        private User testUser;

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


            testUser = User.builder()
                .nickname("testUser")
                .email("testuser@example.com")
                .password("password1234*")
                .build();
            userRepository.save(testUser);
        }

        @Test
        void 관심사_목록을_조회하면_관심사_목록을_반환한다() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/interests")
                    .header("Monew-Request-User-ID", testUser.getId())
                    .param("keyword", "")
                    .param("cursor", "")
                    .param("limit", "10")
                    .param("orderBy", "name")
                    .param("direction", "asc")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4));
        }

        @Test
        void 관심사_이름으로_검색하면_부분일치하는_관심사만_조회된다() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/interests")
                    .header("Monew-Request-User-ID", testUser.getId())
                    .param("keyword", "soccer")
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
                    .header("Monew-Request-User-ID", testUser.getId())
                    .param("keyword", "")
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
                    .header("Monew-Request-User-ID", testUser.getId())
                    .param("keyword", "")
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
        void 잘못된_정렬_기준인_경우_400을_반환한다() throws Exception {
            // Given
            int limit = 10;
            String keyword = "soccer";
            String orderBy = "invalidSort";
            String direction = "asc";

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .header("Monew-Request-User-ID", testUser.getId())
                    .param("keyword", keyword)
                    .param("cursor", "")
                    .param("limit", String.valueOf(limit))
                    .param("orderBy", orderBy)
                    .param("direction", direction)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SORT_FIELD"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 정렬 필드입니다."));
        }

        @Test
        void 잘못된_커서_형식인_경우_400을_반환한다() throws Exception {
            // Given
            int limit = 10;
            String keyword = "soccer";
            String orderBy = "subscriberCount";
            String direction = "asc";
            String cursor = "invalidCursorFormat";

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .header("Monew-Request-User-ID", testUser.getId())
                    .param("keyword", keyword)
                    .param("cursor", cursor)
                    .param("limit", String.valueOf(limit))
                    .param("orderBy", orderBy)
                    .param("direction", direction)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CURSOR_FORMAT"))
                .andExpect(jsonPath("$.message").value("잘못된 커서 형식입니다."));
        }
    }

    @Nested
    @DisplayName("관심사 구독 테스트")
    class InterestSubscribeTests {

        private Interest testInterest;
        private User testUser;

        @Test
        void 관심사를_구독하면_구독된_관심사_응답_DTO를_반환한다() throws Exception {
            // Given
            testInterest = InterestFixture.createInterest();
            interestRepository.save(testInterest);

            UUID savedInterestId = testInterest.getId();

            testUser = User.builder()
                .nickname("testUser")
                .build();
            userRepository.save(testUser);

            long initialSubscriberCount = testInterest.getSubscriberCount();

            // When & Then
            mockMvc.perform(post("/api/interests/{interestId}/subscriptions", savedInterestId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Monew-Request-User-ID", testUser.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.interestId").value(savedInterestId.toString()))
                .andExpect(jsonPath("$.interestName").value(testInterest.getName()))
                .andExpect(jsonPath("$.interestSubscriberCount").value(initialSubscriberCount + 1))
                .andExpect(jsonPath("$.interestKeywords").isArray())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        void 구독하려는_관심사가_없는_경우_404를_반환한다() throws Exception {
            // Given
            UUID nonExistentInterestId = UUID.randomUUID();

            testUser = User.builder()
                .nickname("testUser")
                .build();
            userRepository.save(testUser);

            // When & Then
            mockMvc.perform(post("/api/interests/{interestId}/subscriptions", nonExistentInterestId)
                    .header("Monew-Request-User-ID", testUser.getId())
                    .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INTEREST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("관심사를 찾을 수 없습니다."));
        }
    }
}
