package com.sprint.mission.sb03monewteam1.dto.response;

import java.time.Instant;
import java.util.List;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;

import lombok.Builder;

@Builder
public record CursorPageResponseArticleDto(
        List<ArticleDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        Long totalElements,
        boolean hasNext) {
}
