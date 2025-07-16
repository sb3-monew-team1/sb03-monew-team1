package com.sprint.mission.sb03monewteam1.exception.comment;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class CommentAlreadyLikedException extends CommentException {

  public CommentAlreadyLikedException(UUID commentId, UUID userId) {
    super(ErrorCode.COMMENT_ALREADY_LIKED,
        Map.of("commentId", String.valueOf(commentId), "userId", String.valueOf(userId)));
  }
}
