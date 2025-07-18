package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.CommentLikeActivityDto;
import lombok.Builder;

@Builder
public record CommentLikeActivityCreateEvent(
    CommentLikeActivityDto commentLikeActivityDto
) {

}