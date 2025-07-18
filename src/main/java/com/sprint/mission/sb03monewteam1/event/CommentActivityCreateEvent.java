package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;

public record CommentActivityCreateEvent(
    CommentActivityDto commentActivityDto
){

}
