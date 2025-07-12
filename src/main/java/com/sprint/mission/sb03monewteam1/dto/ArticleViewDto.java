package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ArticleViewDto(
        UUID id,
        UUID userId,
        UUID articleId,
        Instant createdAt) {
}