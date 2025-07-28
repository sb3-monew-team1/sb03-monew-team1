package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommentLikeDto(
    UUID id,
    UUID likedBy,
    Instant createdAt,
    UUID commentId,
    UUID articleId,
    UUID commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    Instant commentCreatedAt
) {

}
