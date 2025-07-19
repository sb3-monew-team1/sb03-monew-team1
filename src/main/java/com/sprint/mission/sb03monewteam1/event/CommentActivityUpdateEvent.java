package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import java.util.UUID;

public record CommentActivityUpdateEvent(
    UUID userId,
    UUID commentId,
    CommentActivityDto commentActivityDto
){

}
