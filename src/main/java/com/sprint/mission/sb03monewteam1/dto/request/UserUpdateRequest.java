package com.sprint.mission.sb03monewteam1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserUpdateRequest(
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해야 합니다")
    String nickname
) {

}
