package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.CommentLikeActivityDto;
import java.util.UUID;
import lombok.Builder;

public record CommentLikeActivityCreateEvent(
    UUID userId,
    CommentLikeActivityDto commentLikeActivityDto
) {

}