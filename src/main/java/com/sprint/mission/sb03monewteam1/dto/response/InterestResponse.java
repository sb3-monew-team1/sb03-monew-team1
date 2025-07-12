package com.sprint.mission.sb03monewteam1.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record InterestResponse(
    UUID id,
    String name,
    List<String> keywords,
    long subscriberCount,
    boolean subscribedByMe
) {}