package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserUpdateRequest;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.sb03monewteam1.exception.user.ForbiddenAccessException;
import com.sprint.mission.sb03monewteam1.exception.user.InvalidEmailOrPasswordException;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.UserMapper;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    @DisplayName("테스트 환경 설정 확인")
    public void setup() {
        assertNotNull(userRepository);
        assertNotNull(userMapper);
        assertNotNull(userService);
    }

    @Nested
    @DisplayName("사용자 생성 테스트")
    class UserCreateTests {

        @Test
        void 사용자를_생성하면_UserDto를_반환해야한다() {
            // Given
            UserRegisterRequest userRegisterRequest = UserFixture.createUserRegisterRequest();
            User savedUser = UserFixture.createUser();
            UserDto expectedUserDto = UserFixture.createUserDto();

            given(userRepository.existsByEmail(userRegisterRequest.email())).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(userMapper.toDto(any(User.class))).willReturn(expectedUserDto);

            // When
            UserDto result = userService.create(userRegisterRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(UserFixture.getDefaultId());
            assertThat(result.email()).isEqualTo(UserFixture.getDefaultEmail());
            assertThat(result.nickname()).isEqualTo(UserFixture.getDefaultNickname());
            assertThat(result.createdAt()).isNotNull();

            then(userRepository).should().existsByEmail(userRegisterRequest.email());
            then(userRepository).should().save(any(User.class));
            then(userMapper).should().toDto(savedUser);
        }

        @Test
        void 회원가입시_이메일이_중복되면_예외가_발생한다() {
            // Given
            UserRegisterRequest userRegisterRequest = UserFixture.createUserRegisterRequestWithDuplicateEmail();

            given(userRepository.existsByEmail(userRegisterRequest.email())).willReturn(true);

            // When & Then
            assertThatThrownBy(
                () -> userService.create(userRegisterRequest)).isInstanceOf(
                EmailAlreadyExistsException.class);

            then(userRepository).should().existsByEmail(userRegisterRequest.email());
            then(userRepository).shouldHaveNoMoreInteractions();
            then(userMapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("사용자 로그인 테스트")
    class UserLoginTests {

        @Test
        void 사용자는_이메일과_비밀번호를_통해_로그인_할_수_있다() {
            // Given
            UserLoginRequest userLoginRequest = UserFixture.createUserLoginRequest();
            User existedUser = UserFixture.createUser();
            UserDto expectedUserDto = UserFixture.createUserDto();

            given(userRepository.findByEmail(userLoginRequest.email())).willReturn(
                Optional.of(existedUser));
            given(userMapper.toDto(any(User.class))).willReturn(expectedUserDto);

            // When
            UserDto result = userService.login(userLoginRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(UserFixture.getDefaultId());
            assertThat(result.email()).isEqualTo(UserFixture.getDefaultEmail());
            assertThat(result.nickname()).isEqualTo(UserFixture.getDefaultNickname());
            assertThat(result.createdAt()).isNotNull();

            then(userRepository).should().findByEmail(userLoginRequest.email());
            then(userMapper).should().toDto(existedUser);
        }

        @Test
        void 존재하지_않는_이메일로_로그인시_예외가_발생한다() {
            // Given
            UserLoginRequest userLoginRequest = UserFixture.createUserLoginRequest();

            given(userRepository.findByEmail(userLoginRequest.email())).willThrow(
                new InvalidEmailOrPasswordException(userLoginRequest.email())
            );

            // When & Then
            assertThatThrownBy(
                () -> userService.login(userLoginRequest)).isInstanceOf(
                InvalidEmailOrPasswordException.class);

            then(userRepository).should().findByEmail(userLoginRequest.email());
            then(userRepository).shouldHaveNoMoreInteractions();
            then(userMapper).shouldHaveNoInteractions();
        }

        @Test
        void 존재하지_않는_비밀번호로_로그인시_예외가_발생한다() {
            // Given
            String correctPassword = "correctPassword123!";
            String wrongPassword = "wrongPassword123!";

            UserLoginRequest userLoginRequest = UserFixture.createUserLoginRequest(
                UserFixture.getDefaultEmail(),
                wrongPassword
            );

            User existedUser = UserFixture.createUser(
                UserFixture.getDefaultEmail(),
                UserFixture.getDefaultNickname(),
                correctPassword
            );

            given(userRepository.findByEmail(userLoginRequest.email())).willReturn(
                Optional.of(existedUser)
            );

            // When & Then
            assertThatThrownBy(
                () -> userService.login(userLoginRequest)).isInstanceOf(
                InvalidEmailOrPasswordException.class);

            then(userRepository).should().findByEmail(userLoginRequest.email());
            then(userRepository).shouldHaveNoMoreInteractions();
            then(userMapper).shouldHaveNoInteractions();
        }

        @Test
        void 논리_삭제된_사용자가_로그인_할_경우_예외가_발생한다() {
            // Given
            UserLoginRequest userLoginRequest = UserFixture.createUserLoginRequest();

            User deleteduser = UserFixture.createUser();
            deleteduser.setDeleted();

            given(userRepository.findByEmail(userLoginRequest.email())).willReturn(
                Optional.of(deleteduser));

            // When & Then
            assertThatThrownBy(
                () -> userService.login(userLoginRequest)).isInstanceOf(
                InvalidEmailOrPasswordException.class);

            then(userRepository).should().findByEmail(userLoginRequest.email());
            then(userRepository).shouldHaveNoMoreInteractions();
            then(userMapper).shouldHaveNoInteractions();

        }
    }

    @Nested
    @DisplayName("사용자 수정 테스트")
    class UserUpdateTests {

        @Test
        void 사용자가_닉네임을_수정하면_DTO를_반환한다() {
            // Given
            UUID requesterId = UserFixture.getDefaultId();
            UUID userId = UserFixture.getDefaultId();
            User existedUser = UserFixture.createUser();
            UserDto existedUserDto = UserFixture.createUserDto(
                userId,
                UserFixture.getDefaultEmail(),
                "newNickname",
                Instant.now()
            );
            UserUpdateRequest userUpdateRequest = UserFixture.userUpdateRequest("newNickname");

            given(userRepository.findById(userId)).willReturn(Optional.of(existedUser));
            given(userMapper.toDto(existedUser)).willReturn(existedUserDto);

            // When
            UserDto result = userService.update(requesterId, userId, userUpdateRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(UserFixture.getDefaultId());
            assertThat(result.email()).isEqualTo(UserFixture.getDefaultEmail());
            assertThat(result.nickname()).isEqualTo(userUpdateRequest.nickname());
            assertThat(result.createdAt()).isNotNull();

            then(userRepository).should().findById(userId);
            then(userMapper).should().toDto(existedUser);
        }

        @Test
        void 다른_사용자의_닉네임을_수정하려_하면_예외가_발생한다() {
            // Given
            UUID targetId = UserFixture.getDefaultId();
            UUID requesterId = UUID.randomUUID();
            User existedUser = UserFixture.createUser();
            UserDto existedUserDto = UserFixture.createUserDto();
            UserUpdateRequest userUpdateRequest = UserFixture.userUpdateRequest("newNickname");

            // When & Then
            assertThatThrownBy(
                () -> userService.update(requesterId, targetId, userUpdateRequest))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("접근 권한이 없습니다")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
        }

        @Test
        void 존재하지_않는_사용자를_수정하면_예외가_발생한다() {
            // Given
            UUID userId = UUID.randomUUID();
            UserUpdateRequest userUpdateRequest = UserFixture.userUpdateRequest("newNickname");

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.update(userId, userId, userUpdateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

}
