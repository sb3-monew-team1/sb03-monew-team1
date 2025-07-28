package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubscriptionDto(
    UUID id,
    UUID interestId,
    String interestName,
    List<String> interestKeywords,
    Long interestSubscriberCount,
    Instant createdAt
) {

}
