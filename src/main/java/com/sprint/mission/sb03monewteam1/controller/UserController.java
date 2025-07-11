package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.controller.api.UserApi;
import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @PostMapping
    public ResponseEntity<UserDto> create(
        @RequestBody @Valid UserRegisterRequest userRegisterRequest
    ) {
        UserDto userDto = userService.create(userRegisterRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }
}
