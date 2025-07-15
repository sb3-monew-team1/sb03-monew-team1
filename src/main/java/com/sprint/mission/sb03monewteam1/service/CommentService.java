package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
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
}