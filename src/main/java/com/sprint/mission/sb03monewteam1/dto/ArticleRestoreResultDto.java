package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ArticleRestoreResultDto(
    Instant restoreDate,
    List<UUID> restoredArticleIds,
    long restoredArticleCount
) {

}
