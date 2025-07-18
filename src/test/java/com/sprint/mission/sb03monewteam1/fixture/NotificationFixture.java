package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import java.util.UUID;

public class NotificationFixture {

    public static Notification createNewArticleNotification() {
        return Notification.builder()
            .content("새로운 기사 등록 알림입니다.")
            .resourceType(ResourceType.interest)
            .resourceId(UUID.randomUUID())
            .isChecked(false)
            .user(UserFixture.createUser())
            .build();
    }

    public static Notification createNewArticleNotification(User user) {
        return Notification.builder()
            .content("새로운 기사 등록 알림입니다.")
            .resourceType(ResourceType.interest)
            .resourceId(UUID.randomUUID())
            .isChecked(false)
            .user(user)
            .build();
    }

    public static Notification createNewArticleNotification(String content) {
        return Notification.builder()
            .content(content)
            .resourceType(ResourceType.interest)
            .resourceId(UUID.randomUUID())
            .isChecked(false)
            .user(UserFixture.createUser())
            .build();
    }

    public static Notification createNewArticleNotification(
        User user,
        Interest interest,
        int articleCount
    ) {
        return Notification.builder()
            .content(String.format("%s와 관련된 기사가 %d건 등록되었습니다.", interest.getName(), articleCount))
            .resourceType(ResourceType.interest)
            .resourceId(interest.getId())
            .isChecked(false)
            .user(user)
            .build();
    }


}