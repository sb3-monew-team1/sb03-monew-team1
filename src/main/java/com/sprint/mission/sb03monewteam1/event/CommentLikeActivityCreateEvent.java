package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.CommentLikeActivityDto;
import java.util.UUID;

public record CommentLikeActivityCreateEvent(
    UUID userId,
    CommentLikeActivityDto commentLikeActivityDto
) {

}