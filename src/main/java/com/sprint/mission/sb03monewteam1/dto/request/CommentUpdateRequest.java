package com.sprint.mission.sb03monewteam1.dto.request;

import lombok.Builder;

@Builder
public record CommentUpdateRequest(
    String content
) {

}
