package com.sprint.mission.sb03monewteam1.dto.response;

import java.util.List;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;

import lombok.Builder;

@Builder
public record CursorPageResponseArticleDto(
        List<ArticleDto> articles,
        String nextCursor,
        boolean hasNext) {
}
