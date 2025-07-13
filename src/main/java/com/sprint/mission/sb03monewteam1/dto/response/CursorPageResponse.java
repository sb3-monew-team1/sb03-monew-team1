package com.sprint.mission.sb03monewteam1.dto.response;

import java.time.Instant;
import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        Long totalElements,
        boolean hasNext
) {

}
