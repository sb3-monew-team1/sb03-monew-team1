package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ArticleViewActivityDto(
    UUID id,
    UUID viewedBy,
    Instant createdAt,
    UUID articleId,
    String source,
    String sourceUrl,
    String articleTitle,
    Instant articlePublishedDate,
    String articleSummary,
    Long articleCommentCount,
    Long articleViewCount
) {

}