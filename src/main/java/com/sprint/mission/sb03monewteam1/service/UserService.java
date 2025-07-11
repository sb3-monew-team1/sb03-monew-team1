package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;

public interface UserService {

    UserDto create(UserRegisterRequest userRegisterRequest);

}
