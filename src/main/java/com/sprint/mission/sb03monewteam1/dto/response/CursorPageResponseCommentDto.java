package com.sprint.mission.sb03monewteam1.dto.response;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import java.time.Instant;
import java.util.List;

public record CursorPageResponseCommentDto(
    List<CommentDto> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {

}
