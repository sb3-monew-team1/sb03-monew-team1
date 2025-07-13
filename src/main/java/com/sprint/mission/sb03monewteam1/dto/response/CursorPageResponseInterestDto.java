package com.sprint.mission.sb03monewteam1.dto.response;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import java.time.Instant;
import java.util.List;

public record CursorPageResponseInterestDto(
    List<InterestDto> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {}
