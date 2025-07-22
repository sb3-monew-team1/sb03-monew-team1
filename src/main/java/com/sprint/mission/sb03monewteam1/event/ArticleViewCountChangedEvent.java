package com.sprint.mission.sb03monewteam1.event;

import java.util.UUID;

public record ArticleViewCountChangedEvent(
    UUID articleId,
    long newViewCount
) {

}