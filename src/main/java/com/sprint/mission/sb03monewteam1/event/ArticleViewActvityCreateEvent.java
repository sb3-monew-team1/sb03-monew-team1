package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import lombok.Builder;

@Builder
public record ArticleViewActvityCreateEvent(
    ArticleViewDto articleViewDto
) {

}