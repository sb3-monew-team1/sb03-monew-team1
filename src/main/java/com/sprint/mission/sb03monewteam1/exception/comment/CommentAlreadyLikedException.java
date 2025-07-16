package com.sprint.mission.sb03monewteam1.exception.comment;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class CommentAlreadyLikedException extends CommentException {

  public CommentAlreadyLikedException(UUID commentLikeId) {
    super(ErrorCode.COMMENT_ALREADY_LIKED, Map.of("commentLikeID", String.valueOf(commentLikeId)));
  }
}
