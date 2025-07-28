package com.sprint.mission.sb03monewteam1.exception.comment;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class CommentException extends CustomException {

    public CommentException(ErrorCode errorCode,
            Map<String, String> details, Throwable cause) {
        super(errorCode, details, cause);
    }

    public CommentException(ErrorCode errorCode,
            Map<String, String> details) {
        super(errorCode, details);
    }

    public CommentException(ErrorCode errorCode,
            Throwable cause) {
        super(errorCode, cause);
    }

    public CommentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
