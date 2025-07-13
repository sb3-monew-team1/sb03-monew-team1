package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.sb03monewteam1.exception.user.InvalidEmailOrPasswordException;
import com.sprint.mission.sb03monewteam1.mapper.UserMapper;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserRegisterRequest userRegisterRequest) {

        log.info("사용자 생성 시작: email={}, nickname={}", userRegisterRequest.email(),
            userRegisterRequest.nickname());

        String email = userRegisterRequest.email();
        String nickname = userRegisterRequest.nickname();
        String password = userRegisterRequest.password();

        if (userRepository.existsByEmail(email)) {
            log.warn("중복된 이메일로 회원가입 시도: email={}", email);
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
            .email(email)
            .nickname(nickname)
            .password(password)
            .build();

        User savedUser = userRepository.save(user);

        log.info("사용자 생성 완료: id={}, email={}, nickname={}",
            savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto login(UserLoginRequest userLoginRequest) {
        String email = userLoginRequest.email();
        String password = userLoginRequest.password();

        log.info("로그인 인증 시작 - email={}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(
                () -> {
                    log.warn("로그인 실패 - 존재하지 않는 이메일: {}", email);
                    return new InvalidEmailOrPasswordException(email);
                });

        if (!user.getPassword().equals(password) || user.isDeleted()) {
            log.warn("로그인 실패 - 비밀번호 불일치, 입력한 비밀번호: {}", password);
            throw new InvalidEmailOrPasswordException(email);
        }

        log.info("로그인 성공 - email={}, nickname={}", user.getEmail(), user.getNickname());

        return userMapper.toDto(user);
    }
}
