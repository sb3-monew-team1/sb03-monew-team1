package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.CommentUpdateRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import java.time.Instant;
import java.util.UUID;

public interface CommentService {

    CommentDto create(CommentRegisterRequest commentRegisterRequest);

    CursorPageResponse<CommentDto> getCommentsWithCursorBySort(
        UUID articleId,
        String cursor,
        Instant nextAfter,
        int size,
        String sortBy,
        String sortDirection
    );

    CommentDto update(UUID commentId, UUID userId, CommentUpdateRequest commentUpdateRequest);

    Comment delete(UUID commentId, UUID userId);
}