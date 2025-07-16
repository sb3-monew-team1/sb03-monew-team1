package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InterestFixture {

    private static final String DEFAULT_NAME = "football activity";
    private static final String SIMILAR_NAME = "football activities";
    private static final List<String> DEFAULT_KEYWORDS = List.of("스포츠", "해외축구");

    // 기본 요청 DTO
    public static InterestRegisterRequest createInterestRegisterRequest() {
        return InterestRegisterRequest.builder()
            .name(DEFAULT_NAME)
            .keywords(DEFAULT_KEYWORDS)
            .build();
    }

    // 이름 빈값 요청 DTO
    public static InterestRegisterRequest createInterestRegisterRequestWithEmptyName() {
        return InterestRegisterRequest.builder()
            .name("")
            .keywords(DEFAULT_KEYWORDS)
            .build();
    }

    // 유사한 이름을 가진 요청 DTO (80% 이상 유사한 이름)
    public static InterestRegisterRequest createInterestRegisterRequestWithSimilarName() {
        return InterestRegisterRequest.builder()
            .name(SIMILAR_NAME)
            .keywords(DEFAULT_KEYWORDS)
            .build();
    }

    // 여러 DTO를 한 번에 생성
    public static List<InterestDto> createInterestDtoList() {
        InterestDto interestDto1 = createInterestResponseDto("football club", List.of("sports", "club"), 150L);
        InterestDto interestDto2 = createInterestResponseDto("soccer", List.of("football", "ball"), 200L);
        InterestDto interestDto3 = createInterestResponseDto("aesthetic", List.of("spa", "cosmetics"), 100L);
        InterestDto interestDto4 = createInterestResponseDto("beauty", List.of("massage", "spa"), 50L);

        return List.of(interestDto1, interestDto2, interestDto3, interestDto4);
    }

    // 응답 DTO
    public static InterestDto createInterestResponseDto() {
        return createInterestResponseDto(DEFAULT_NAME, DEFAULT_KEYWORDS, 0L);
    }

    // 이름과 키워드를 받는 DTO 생성
    public static InterestDto createInterestResponseDto(String name, List<String> keywords) {
        return createInterestResponseDto(name, keywords, 0L);
    }

    // 이름, 키워드, 구독자 수를 받는 DTO 생성
    public static InterestDto createInterestResponseDto(String name, List<String> keywords,
        long subscriberCount) {
        return InterestDto.builder()
            .id(UUID.randomUUID())
            .name(name)
            .keywords(keywords)
            .subscriberCount(subscriberCount)
            .subscribedByMe(false)
            .build();
    }

    // 기본 Entity 생성
    public static Interest createInterest(String name, Long subscriberCount, List<String> keywords) {
        Interest.InterestBuilder interestBuilder = Interest.builder()
            .name(name)
            .subscriberCount(subscriberCount);

        if (keywords != null && !keywords.isEmpty()) {
            List<InterestKeyword> interestKeywords = keywords.stream()
                .map(keyword -> InterestKeyword.builder().keyword(keyword).build())
                .toList();
            interestBuilder.keywords(interestKeywords);
        }

        return interestBuilder.build();
    }

    public static Interest createInterest() {
        return createInterest(DEFAULT_NAME, 150000L, DEFAULT_KEYWORDS);
    }
}
