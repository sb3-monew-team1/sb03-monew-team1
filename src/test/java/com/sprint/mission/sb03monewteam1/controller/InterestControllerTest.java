package com.sprint.mission.sb03monewteam1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestSimilarityException;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import com.sprint.mission.sb03monewteam1.service.InterestService;

import com.sprint.mission.sb03monewteam1.service.UserService;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;
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
import static org.mockito.ArgumentMatchers.*;
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

    @MockitoBean
    private UserService userService;

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

            given(interestService.create(any(InterestRegisterRequest.class)))
                .willThrow(new IllegalArgumentException("관심사 이름은 필수입니다."));

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

            given(interestService.create(any(InterestRegisterRequest.class)))
                .willThrow(new IllegalArgumentException("키워드는 최소 1개 이상"));

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

        @Test
        void 관심사_목록을_조회하면_관심사_목록을_반환한다() throws Exception {
            // Given
            List<InterestDto> interestDtos = InterestFixture.createInterestDtoList();
            CursorPageResponse<InterestDto> responseDto = new CursorPageResponse<>(
                interestDtos, null, null, 10, 4L, false
            );

            given(interestService.getInterests(
                anyString(), anyString(), anyInt(), anyString(), anyString()))
                .willReturn(responseDto);

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("keyword", "")
                    .param("cursor", "")
                    .param("limit", "10")
                    .param("orderBy", "name")
                    .param("direction", "asc")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].name").value("football club"))
                .andExpect(jsonPath("$.content[1].name").value("soccer"))
                .andExpect(jsonPath("$.content[2].name").value("aesthetic"))
                .andExpect(jsonPath("$.content[3].name").value("beauty"));
        }

        @Test
        void 관심사를_이름으로_검색하면_부분일치하는_관심사만_조회된다() throws Exception {
            // Given
            List<InterestDto> interestDtos = List.of(
                InterestFixture.createInterestResponseDto("football", List.of("club", "sport")),
                InterestFixture.createInterestResponseDto("soccer", List.of("ball", "sports"))
            );
            CursorPageResponse<InterestDto> responseDto = new CursorPageResponse<>(
                interestDtos, null, null, 10, 2L, false
            );

            given(interestService.getInterests(
                eq("football"), anyString(), eq(10), eq("name"), eq("asc")))
                .willReturn(responseDto);

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("keyword", "football")
                    .param("cursor", "")
                    .param("limit", "10")
                    .param("orderBy", "name")
                    .param("direction", "asc")
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
            List<InterestDto> interestDtos = Arrays.asList(
                InterestDto.builder().id(UUID.randomUUID()).name("soccer").subscriberCount(200L).build(),
                InterestDto.builder().id(UUID.randomUUID()).name("football club").subscriberCount(150L).build(),
                InterestDto.builder().id(UUID.randomUUID()).name("aesthetic").subscriberCount(100L).build(),
                InterestDto.builder().id(UUID.randomUUID()).name("beauty").subscriberCount(50L).build()
            );

            CursorPageResponse<InterestDto> responseDto = CursorPageResponse.<InterestDto>builder()
                .content(interestDtos)
                .nextCursor("50")
                .nextAfter(Instant.now())
                .size(10)
                .totalElements(4L)
                .hasNext(false)
                .build();

            given(interestService.getInterests(
                anyString(), anyString(), eq(10), eq("subscriberCount"), eq("desc")))
                .willReturn(responseDto);

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("keyword", "")
                    .param("cursor", "")
                    .param("orderBy", "subscriberCount")
                    .param("direction", "desc")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("soccer"))
                .andExpect(jsonPath("$.content[1].name").value("football club"))
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
            List<InterestDto> interestDtos = Arrays.asList(
                InterestDto.builder().id(UUID.randomUUID()).name("aesthetic").build(),
                InterestDto.builder().id(UUID.randomUUID()).name("beauty").build(),
                InterestDto.builder().id(UUID.randomUUID()).name("football club").build(),
                InterestDto.builder().id(UUID.randomUUID()).name("soccer").build()
            );

            CursorPageResponse<InterestDto> responseDto = CursorPageResponse.<InterestDto>builder()
                .content(interestDtos)
                .nextCursor("50")
                .nextAfter(Instant.now())
                .size(10)
                .totalElements(4L)
                .hasNext(false)
                .build();

            given(interestService.getInterests(
                anyString(), anyString(), eq(10), eq("name"), eq("asc")))
                .willReturn(responseDto);

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("keyword", "")
                    .param("cursor", "")
                    .param("orderBy", "name")
                    .param("direction", "asc")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("aesthetic"))
                .andExpect(jsonPath("$.content[1].name").value("beauty"))
                .andExpect(jsonPath("$.content[2].name").value("football club"))
                .andExpect(jsonPath("$.content[3].name").value("soccer"));
        }

        @Test
        void 잘못된_커서_형식인_경우_400을_반환한다() throws Exception {
            // Given
            String invalidCursor = "invalidCursor";
            String keyword = "soccer";
            int limit = 10;

            given(interestService.getInterests(any(), any(), anyInt(), any(), any()))
                .willThrow(new InvalidCursorException("잘못된 커서 형식입니다."));

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("keyword", keyword)
                    .param("cursor", invalidCursor)
                    .param("limit", String.valueOf(limit))
                    .param("orderBy", "name")
                    .param("direction", "asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CURSOR_FORMAT"))
                .andExpect(jsonPath("$.message").value("잘못된 커서 형식입니다."));
        }

        @Test
        void 잘못된_정렬_기준인_경우_400을_반환한다() throws Exception {
            // Given
            String invalidOrderBy = "invalidOrder";
            String keyword = "soccer";
            int limit = 10;

            given(interestService.getInterests(any(), any(), anyInt(), any(), any()))
                .willThrow(new InvalidSortOptionException("지원하지 않는 정렬 필드입니다."));

            // When & Then
            mockMvc.perform(get("/api/interests")
                    .param("keyword", keyword)
                    .param("cursor", "")
                    .param("limit", String.valueOf(limit))
                    .param("orderBy", invalidOrderBy)
                    .param("direction", "asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SORT_FIELD"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 정렬 필드입니다."));
        }

        @Test
        void 관심사를_구독하면_200을_반환한다() throws Exception {
            // Given
            UUID interestId = UUID.randomUUID();
            Interest interest = InterestFixture.createInterest();
            User testUser = UserFixture.createUser();
            Subscription subscription = new Subscription(testUser, interest);

            SubscriptionDto expectedResponse = new SubscriptionDto(
                subscription.getId(),
                interest.getId(),
                interest.getName(),
                interest.getKeywords().stream()
                    .map(InterestKeyword::getKeyword)
                    .collect(Collectors.toList()),
                interest.getSubscriberCount(),
                subscription.getCreatedAt()
            );

            given(subscriptionService.subscribe(testUser, interest)).willReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
                    .header("Monew-Request-User-ID", testUser.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interestId").value(interestId.toString()))
                .andExpect(jsonPath("$.interestName").value(interest.getName()))
                .andExpect(jsonPath("$.interestKeywords").value(interest.getKeywords()))
                .andExpect(jsonPath("$.interestSubscriberCount").value(interest.getSubscriberCount()))
                .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        void 구독하려는_관심사가_없는_경우_404를_반환한다() throws Exception {
            // Given
            UUID nonExistentInterestId = UUID.randomUUID();
            User testUser = UserFixture.createUser();

            given(interestService.findById(nonExistentInterestId))
                .willThrow(new InterestNotFoundException("관심사를 찾을 수 없습니다."));

            // When & Then
            mockMvc.perform(post("/api/interests/{interestId}/subscriptions", nonExistentInterestId)
                    .header("Monew-Request-User-ID", testUser.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("관심사를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.status").value(404));
        }
    }
}
