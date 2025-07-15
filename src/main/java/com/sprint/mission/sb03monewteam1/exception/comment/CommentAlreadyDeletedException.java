package com.sprint.mission.sb03monewteam1.exception.comment;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class CommentAlreadyDeletedException extends CommentException {

  public CommentAlreadyDeletedException(UUID commentId) {
    super(ErrorCode.COMMENT_ALREADY_DELETED, Map.of("commentID", String.valueOf(commentId)));
  }
}
