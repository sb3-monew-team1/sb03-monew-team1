package com.sprint.mission.sb03monewteam1.event;

import java.util.UUID;

public record CommentLikeCountActivityUpdateEvent(
    UUID commentId,
    long newLikeCount
) {

}