package com.sprint.mission.sb03monewteam1.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record InterestDto(
    UUID id,
    String name,
    List<String> keywords,
    long subscriberCount,
    boolean subscribedByMe
) {}