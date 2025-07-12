package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.controller.api.UserApi;
import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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

        log.info("회원가입 요청: email={}, nickname={}",
            userRegisterRequest.email(), userRegisterRequest.nickname());

        UserDto userDto = userService.create(userRegisterRequest);

        log.info("회원가입 완료: id={}, email={}, nickname={}",
            userDto.id(), userDto.email(), userDto.nickname());

        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(
        @RequestBody @Valid UserLoginRequest userLoginRequest
    ) {
        log.info("로그인 요청: email={}", userLoginRequest.email());

        UserDto userDto = userService.login(userLoginRequest);

        log.info("로그인 완료: id={}, email={}, nickname={}",
            userDto.id(), userDto.email(), userDto.nickname());

        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }
}
