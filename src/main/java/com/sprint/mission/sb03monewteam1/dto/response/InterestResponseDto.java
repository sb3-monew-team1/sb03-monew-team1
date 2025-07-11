package com.sprint.mission.sb03monewteam1.dto.response;

import java.util.List;
import java.util.UUID;

public record InterestResponseDto(
    UUID id,
    String name,
    List<String> keywords,
    long subscriberCount,
    boolean subscribedByMe
) {}