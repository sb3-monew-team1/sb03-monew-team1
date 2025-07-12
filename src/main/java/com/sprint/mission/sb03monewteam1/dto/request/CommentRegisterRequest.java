package com.sprint.mission.sb03monewteam1.dto.request;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CommentRegisterRequest(
        UUID articleId,
        UUID userId,
        String content
) {

}