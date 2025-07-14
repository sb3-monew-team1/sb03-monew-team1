package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.controller.api.UserApi;
import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserUpdateRequest;
import com.sprint.mission.sb03monewteam1.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Override
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(
        @PathVariable UUID userId,
        @RequestBody @Valid UserUpdateRequest request,
        HttpServletRequest httpServletRequest
    ) {
        log.info("사용자 정보 수정 요청: userId={}, nickname={}", userId, request.nickname());

        UUID requestUserId = (UUID) httpServletRequest.getAttribute("userId");
        log.info("Monew-Request-User-ID: {}", requestUserId);

        UserDto userDto = userService.update(requestUserId, userId, request);

        log.info("사용자 정보 수정 완료: id={}, nickname={}", userDto.id(), userDto.nickname());

        return ResponseEntity.ok(userDto);
    }
}
