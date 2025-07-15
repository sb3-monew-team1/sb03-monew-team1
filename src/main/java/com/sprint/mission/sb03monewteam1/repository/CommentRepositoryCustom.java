package com.sprint.mission.sb03monewteam1.repository;

import com.sprint.mission.sb03monewteam1.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {

    List<Comment> findCommentsWithCursorBySort(
        UUID articleId,
        String cursor,
        Instant nextAfter,
        int limit,
        String sortBy,
        String sortDirection
    );
}
