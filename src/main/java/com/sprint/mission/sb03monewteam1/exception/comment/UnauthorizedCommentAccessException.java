package com.sprint.mission.sb03monewteam1.exception.comment;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;

public class UnauthorizedCommentAccessException extends CommentException {

    public UnauthorizedCommentAccessException() {
        super(ErrorCode.FORBIDDEN_ACCESS);
    }
}
