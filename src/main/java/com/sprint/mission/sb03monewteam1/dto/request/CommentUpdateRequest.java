package com.sprint.mission.sb03monewteam1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CommentUpdateRequest(

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(min = 1, max = 500, message = "댓글 내용은 1자 이상 500자 이하여야 합니다.")
    String content
) {

}
