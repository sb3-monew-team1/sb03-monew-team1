package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import java.util.UUID;

public record CommentActivityCreateEvent(
    UUID userId,
    CommentActivityDto commentActivityDto
){

}
