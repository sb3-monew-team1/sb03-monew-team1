package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.User;

public interface NotificationService {

    void create(User user, Interest interest, int articleCount);

}
