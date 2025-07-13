package com.sprint.mission.sb03monewteam1.repository;

import com.sprint.mission.sb03monewteam1.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CommentRepositoryImpl implements CommentRepositoryCustom{

    @Override
    public List<Comment> findCommentsWithCursorBySort(UUID articleId, String cusrosr,
        Instant nextAfter, int limit, String sortBy, String sortDirection) {
        return List.of();
    }

    @Override
    public Long countByArticleId(UUID articleId) {
        return 0L;
    }
}
