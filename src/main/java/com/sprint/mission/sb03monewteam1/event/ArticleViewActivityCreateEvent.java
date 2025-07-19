package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.ArticleViewActivityDto;
import java.util.UUID;

public record ArticleViewActivityCreateEvent(
    UUID userId,
    ArticleViewActivityDto articleViewActivityDto
) {

}