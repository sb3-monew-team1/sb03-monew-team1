package com.sprint.mission.sb03monewteam1.dto.request;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommentCursorRequest(

    UUID articleId,
    String cursor,
    Instant after,
    int limit,
    String orderBy,
    String direction
) {
}

