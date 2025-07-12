package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ArticleDto(
        UUID id,
        String source,
        String sourceUrl,
        String title,
        Instant publishDate,
        String summary,
        Long viewCount,
        Long commentCount,
        List<String> interests,
        Instant createdAt) {
}