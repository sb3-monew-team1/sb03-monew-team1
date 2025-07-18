package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import lombok.Builder;

@Builder
public record CommentActivityCreateEvent(
    CommentDto commentDto
) {

}
