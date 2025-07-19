package com.sprint.mission.sb03monewteam1.event;

import java.util.UUID;

public record CommentLikeActivityDeleteEvent(
    UUID id,
    UUID commentId
) {

}