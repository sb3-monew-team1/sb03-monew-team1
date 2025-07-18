package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.ArticleViewActivityDto;
import lombok.Builder;

@Builder
public record ArticleViewActivityCreateEvent(
    ArticleViewActivityDto articleViewActivityDto
) {

}