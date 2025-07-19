package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.UserActivityDto;
import java.util.UUID;

public interface UserActivityService {

    UserActivityDto getUserActivity(UUID userId);
}
