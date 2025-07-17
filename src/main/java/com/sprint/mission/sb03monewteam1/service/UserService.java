package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserUpdateRequest;
import java.util.UUID;

public interface UserService {

    UserDto create(UserRegisterRequest userRegisterRequest);

    UserDto login(UserLoginRequest userLoginRequest);

    UserDto update(UUID requestHeaderUserId, UUID userId, UserUpdateRequest request);

    void delete(UUID requestHeaderUserId, UUID userId);

    void deleteHard(UUID requestHeaderUserId, UUID userId);

}
