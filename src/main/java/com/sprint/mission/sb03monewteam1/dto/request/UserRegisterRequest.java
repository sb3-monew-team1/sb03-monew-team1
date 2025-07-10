package com.sprint.mission.sb03monewteam1.dto.request;

import lombok.Builder;

@Builder
public record UserRegisterRequest(
    String email,
    String nickname,
    String password
) {

}
