package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentLikeEvent {

    private final User user;

    private final Comment comment;
}
