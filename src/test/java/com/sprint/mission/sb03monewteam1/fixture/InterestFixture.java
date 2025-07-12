package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponse;

import java.util.List;
import java.util.UUID;

public class InterestFixture {

    private static final String DEFAULT_NAME = "축구";
    private static final List<String> DEFAULT_KEYWORDS = List.of("스포츠", "해외축구");
    private static final UUID DEFAULT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    // 기본 요청 DTO
    public static InterestRegisterRequest createInterestCreateRequest() {
        return InterestRegisterRequest.builder()
            .name(DEFAULT_NAME)
            .keywords(DEFAULT_KEYWORDS)
            .build();
    }

    // 이름 빈값 요청 DTO
    public static InterestRegisterRequest createRequestWithEmptyName() {
        return InterestRegisterRequest.builder()
            .name("")
            .keywords(DEFAULT_KEYWORDS)
            .build();
    }

    // 응답 DTO
    public static InterestResponse createInterestResponseDto() {
        return InterestResponse.builder()
            .id(DEFAULT_ID)
            .name(DEFAULT_NAME)
            .keywords(DEFAULT_KEYWORDS)
            .subscriberCount(0L)
            .subscribedByMe(false)
            .build();
    }
}
