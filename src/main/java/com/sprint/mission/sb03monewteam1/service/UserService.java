package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserUpdateRequest;
import java.util.UUID;

public interface UserService {

    UserDto createUser(UserRegisterRequest userRegisterRequest);

    UserDto login(UserLoginRequest userLoginRequest);

    UserDto updateUser(UUID requestHeaderUserId, UUID userId, UserUpdateRequest request);

    void deleteUser(UUID requestHeaderUserId, UUID userId);

    void deleteHardUser(UUID requestHeaderUserId, UUID userId);

}
