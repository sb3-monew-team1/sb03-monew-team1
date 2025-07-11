package com.sprint.mission.sb03monewteam1.dto.request;

import java.util.List;

public record InterestCreateRequestDto(
    String name,
    List<String> keywords
) {}
