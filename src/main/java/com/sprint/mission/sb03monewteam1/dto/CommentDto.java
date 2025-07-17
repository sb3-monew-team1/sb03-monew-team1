package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record CommentDto(
    UUID id,
    Instant createdAt,
    UUID articleId,
    UUID userId,
    String userNickname,
    String content,
    Long likeCount,
    Boolean likedByMe
) {

}