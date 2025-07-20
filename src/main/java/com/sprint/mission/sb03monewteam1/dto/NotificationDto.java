package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    boolean confirmed,
    UUID userId,
    String content,
    String resourceType,
    UUID resourceId
) {

}
