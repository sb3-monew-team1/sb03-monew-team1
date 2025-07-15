package com.sprint.mission.sb03monewteam1.exception.comment;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class CommentNotFoundException extends CommentException {

    public CommentNotFoundException(UUID commentId) {
        super(ErrorCode.COMMENT_NOT_FOUND, Map.of("commentID", String.valueOf(commentId)));
    }
}
