package com.sprint.mission.sb03monewteam1.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record UserDto(
    Long id,
    String email,
    String nickname,
    Instant createdAt
) {

}
