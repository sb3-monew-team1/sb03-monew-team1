package com.sprint.mission.sb03monewteam1.exception.comment;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class CommentLikeNotFoundException extends CommentException {

  public CommentLikeNotFoundException(UUID userId, UUID commentId) {
    super(ErrorCode.COMMENT_LIKE_NOT_FOUND,
        Map.of("userId", String.valueOf(userId), "commentId", String.valueOf(commentId)));
  }
}
