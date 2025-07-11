package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.UserMapper;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
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
                IllegalArgumentException.class);

            then(userRepository).should().existsByEmail(userRegisterRequest.email());
            then(userRepository).shouldHaveNoMoreInteractions();
            then(userMapper).shouldHaveNoInteractions();
        }
    }

}
