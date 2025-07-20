package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void createNewArticleNotification(User user, Interest interest, int articleCount);

    void createCommentLikeNotification(User user, Comment comment);

    void confirm(UUID notificationId, UUID userId);

    void confirmAll(UUID userId);

    CursorPageResponse<NotificationDto> getUncheckedNotifications(
        UUID userId,
        String cursor,
        Instant after,
        int limit
    );
}
