package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "resourceType", expression = "java(notification.getResourceType().name())")
    @Mapping(target = "confirmed", source = "checked")
    NotificationDto toDto(Notification notification);
}
