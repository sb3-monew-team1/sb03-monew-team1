package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;

import lombok.Builder;

@Builder
public record CollectedArticleDto(
        String source,
        String sourceUrl,
        String title,
        Instant publishDate,
        String summary,
        String rawContent,
        String imageUrl) {
}
