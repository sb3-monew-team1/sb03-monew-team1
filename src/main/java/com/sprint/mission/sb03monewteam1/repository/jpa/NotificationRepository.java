package com.sprint.mission.sb03monewteam1.repository.jpa;

import com.sprint.mission.sb03monewteam1.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

}
