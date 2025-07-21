package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.dto.ResourceType;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import com.sprint.mission.sb03monewteam1.entity.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationFixture {

    private static final UUID DEFAULT_NOTIFICATION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String DEFAULT_CONTENT = "테스트 알림 내용입니다.";
    private static final ResourceType DEFAULT_RESOURCE_TYPE = ResourceType.INTEREST;
    private static final UUID DEFAULT_RESOURCE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final boolean DEFAULT_IS_CHECKED = false;
    private static final Instant DEFAULT_CREATED_AT = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant DEFAULT_UPDATED_AT = Instant.parse("2024-01-01T00:00:00Z");


    public static Notification createNewArticleNotification() {
        return Notification.builder()
            .content("관심사에 새 기사가 등록되었습니다.")
            .resourceType(ResourceType.INTEREST)
            .resourceId(UUID.randomUUID())
            .isChecked(false)
            .user(UserFixture.createUser())
            .build();
    }

    public static Notification createNewArticleNotification(User user) {
        return Notification.builder()
            .content("새로운 기사 등록 알림입니다.")
            .resourceType(ResourceType.INTEREST)
            .resourceId(UUID.randomUUID())
            .isChecked(false)
            .user(user)
            .build();
    }

    public static Notification createNewArticleNotification(String content) {
        return Notification.builder()
            .content(content)
            .resourceType(ResourceType.INTEREST)
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
            .resourceType(ResourceType.INTEREST)
            .resourceId(interest.getId())
            .isChecked(false)
            .user(user)
            .build();
    }

    public static NotificationDto createNotificationDtoWithConfirmed(Notification notification, boolean confirmed) {
        return NotificationDto.builder()
            .id(notification.getId())
            .content(notification.getContent())
            .createdAt(notification.getCreatedAt())
            .confirmed(confirmed)
            .resourceId(notification.getResourceId())
            .resourceType(String.valueOf(notification.getResourceType()))
            .userId(notification.getUser().getId())
            .updatedAt(notification.getUpdatedAt())
            .build();
    }

    public static Notification createUncheckedNotification(User user) {
        return Notification.builder()
            .content(DEFAULT_CONTENT)
            .resourceType(DEFAULT_RESOURCE_TYPE)
            .resourceId(DEFAULT_RESOURCE_ID)
            .isChecked(DEFAULT_IS_CHECKED)
            .user(user)
            .build();
    }

    public static Notification createCheckedNotification(User user) {
        Notification notification = Notification.builder()
            .content(DEFAULT_CONTENT)
            .resourceType(DEFAULT_RESOURCE_TYPE)
            .resourceId(DEFAULT_RESOURCE_ID)
            .isChecked(true)
            .user(user)
            .build();
        notification.markAsChecked();
        return notification;
    }

    public static Notification createNotificationWithContent(User user, String content) {
        return Notification.builder()
            .content(content)
            .resourceType(DEFAULT_RESOURCE_TYPE)
            .resourceId(DEFAULT_RESOURCE_ID)
            .isChecked(DEFAULT_IS_CHECKED)
            .user(user)
            .build();
    }

    public static List<Notification> createUncheckedNotifications(User user, int count) {
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            notifications.add(createNotificationWithContent(user, "테스트 알림 " + (i + 1)));
        }
        return notifications;
    }

    public static NotificationDto createNotificationDto() {
        return new NotificationDto(
            DEFAULT_NOTIFICATION_ID,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_IS_CHECKED,
            UserFixture.getDefaultId(),
            DEFAULT_CONTENT,
            DEFAULT_RESOURCE_TYPE.name(),
            DEFAULT_RESOURCE_ID
        );
    }

    public static NotificationDto createNotificationDto(UUID id, String content, boolean isChecked) {
        return new NotificationDto(
            id,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            isChecked,
            UserFixture.getDefaultId(),
            content,
            DEFAULT_RESOURCE_TYPE.name(),
            DEFAULT_RESOURCE_ID
        );
    }

    public static UUID getDefaultNotificationId() {
        return DEFAULT_NOTIFICATION_ID;
    }

    public static String getDefaultContent() {
        return DEFAULT_CONTENT;
    }

    public static ResourceType getDefaultResourceType() {
        return DEFAULT_RESOURCE_TYPE;
    }

    public static UUID getDefaultResourceId() {
        return DEFAULT_RESOURCE_ID;
    }

}