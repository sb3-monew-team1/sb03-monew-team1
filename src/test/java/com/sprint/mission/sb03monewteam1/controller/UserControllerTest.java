package com.sprint.mission.sb03monewteam1.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserUpdateRequest;
import com.sprint.mission.sb03monewteam1.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.sb03monewteam1.exception.user.ForbiddenAccessException;
import com.sprint.mission.sb03monewteam1.exception.user.InvalidEmailOrPasswordException;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.service.UserService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
@DisplayName("UserController 테스트")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    @DisplayName("테스트 환경 설정 확인")
    void setup() {
        assertNotNull(mockMvc);
        assertNotNull(objectMapper);
        assertNotNull(userService);
    }

    @Nested
    @DisplayName("사용자 생성 태스트")
    class UserCreateTests {

        @Test
        void 사용자를_생성하면_201이_반한되어야_한다() throws Exception {
            // Given
            UserRegisterRequest userRegisterRequest = UserFixture.createUserRegisterRequest();
            UserDto userDto = UserFixture.createUserDto();

            given(userService.create(userRegisterRequest)).willReturn(userDto);

            // When & Then
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(userRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(UserFixture.getDefaultEmail()))
                .andExpect(jsonPath("$.nickname").value(UserFixture.getDefaultNickname()));
        }

        @Test
        void 중복된_이메일로_회원가입시_409를_반환해야_한다() throws Exception {
            // Given
            UserRegisterRequest userRegisterRequest = UserFixture.createUserRegisterRequestWithDuplicateEmail();
            UserDto userDto = UserFixture.createUserDto();

            given(userService.create(userRegisterRequest))
                .willThrow(new EmailAlreadyExistsException(UserFixture.getDefaultEmail()));

            // When & Then
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(userRegisterRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));
        }

        @Test
        void 회원가입_시_이메일이_빈_문자열이면_400을_반환해야_한다() throws Exception {
            // Given
            UserRegisterRequest request = UserRegisterRequest.builder()
                .email("")
                .nickname("testUser")
                .password("!password123")
                .build();

            // When & Then
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.email").value("이메일은 필수입니다"));
        }

        @Test
        void 회원가입_시_닉네임이_빈_문자열이면_400을_반환해야_한다() throws Exception {
            // Given
            UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .nickname(" ")
                .password("!password123")
                .build();

            // When & Then
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.nickname").value("닉네임은 필수입니다"));
        }

        @Test
        void 회원가입_시_비밀번호에_영문자가_없으면_400을_반환해야_한다() throws Exception {
            // Given
            UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .nickname("testUser")
                .password("!123456")
                .build();

            // When & Then
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.password").value(
                    "비밀번호는 6자 이상 20자 이하이며, 최소 하나의 영문자, 숫자, 특수문자(@$!%*?&)를 포함해야 합니다"));
        }
    }

    @Nested
    @DisplayName("사용자 로그인 테스트")
    class UserLoginTests {

        @Test
        void 로그인에_성공하면_200이_반환되어야_한다() throws Exception {
            // Given
            UserLoginRequest userLoginRequest = UserFixture.createUserLoginRequest();
            UserDto userDto = UserFixture.createUserDto();

            given(userService.login(userLoginRequest)).willReturn(userDto);

            // When & Then
            mockMvc.perform(post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(UserFixture.getDefaultEmail()))
                .andExpect(jsonPath("$.nickname").value(UserFixture.getDefaultNickname()));

        }

        @Test
        void 등록되지_않은_이메일로_로그인_시_401을_반환해야_한다() throws Exception {
            //Given
            UserLoginRequest userLoginRequest = UserFixture.createUserLoginRequest();
            UserDto userDto = UserFixture.createUserDto();

            given(userService.login(userLoginRequest))
                .willThrow(new InvalidEmailOrPasswordException(UserFixture.getDefaultEmail()));

            //When & Then
            mockMvc.perform(post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void 틀린_비밀번호로_로그인_시_401을_반환해야_한다() throws Exception {
            //Given
            UserLoginRequest userLoginRequest = UserFixture.createUserLoginRequest();
            UserDto userDto = UserFixture.createUserDto();

            given(userService.login(userLoginRequest))
                .willThrow(new InvalidEmailOrPasswordException(UserFixture.getDefaultEmail()));

            //When & Then
            mockMvc.perform(post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void 로그인_시_이메일이_빈_칸_이면_400을_반환해야_한다() throws Exception {
            // Given
            UserLoginRequest request = UserLoginRequest.builder()
                .email("")
                .password("!password123")
                .build();

            // When & Then
            mockMvc.perform(post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.email").value("이메일은 필수입니다"));
        }

    }

    @Nested
    @DisplayName("사용자 정보 수정 테스트")
    class UserUpdateTests {

        @Test
        void 사용자_정보_수정에_성공하면_200을_반환해야_한다() throws Exception {
            // Given
            UUID requestHeaderUserId = UserFixture.getDefaultId();
            UUID userId = UserFixture.getDefaultId();
            UserUpdateRequest userUpdateRequest = UserFixture.userUpdateRequest("newNickname");
            UserDto userDto = UserFixture.createUserDto(
                userId,
                UserFixture.getDefaultEmail(),
                userUpdateRequest.nickname(),
                Instant.now()
            );

            given(userService.update(requestHeaderUserId, userId, userUpdateRequest)).willReturn(
                userDto);

            // When & Then
            mockMvc.perform(patch("/api/users/{userId}", userId)
                    .requestAttr("userId", requestHeaderUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(UserFixture.getDefaultEmail()))
                .andExpect(jsonPath("$.nickname").value("newNickname"));
        }

        @Test
        void 수정할_닉네임이_공백이면_400을_반환해야_한다() throws Exception {
            // Given
            UUID userId = UserFixture.getDefaultId();
            UserUpdateRequest userUpdateRequest = UserFixture.userUpdateRequest("");

            // When & Then
            mockMvc.perform(patch("/api/users/{userId}", userId)
                    .requestAttr("userId", UserFixture.getDefaultId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(userUpdateRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void 타인의_정보를_수정하려_하면_403을_반환해야_한다() throws Exception {
            // Given
            UUID requestHeaderUserId = UserFixture.getDefaultId();
            UUID userId = UUID.randomUUID();
            UserUpdateRequest userUpdateRequest = UserFixture.userUpdateRequest("newNickname");

            given(userService.update(requestHeaderUserId, userId, userUpdateRequest))
                .willThrow(new ForbiddenAccessException("권한이 없습니다"));

            // When & Then
            mockMvc.perform(patch("/api/users/{userId}", userId)
                    .requestAttr("userId", requestHeaderUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(userUpdateRequest)))
                .andExpect(status().isForbidden());
        }

        @Test
        void 존재하지_않는_사용자의_정보를_수정하면_404를_반환해야_한다() throws Exception {
            // Given
            UUID requestHeaderUserId = UserFixture.getDefaultId();
            UUID userId = UserFixture.getDefaultId();
            UserUpdateRequest userUpdateRequest = UserFixture.userUpdateRequest("newNickname");
            UserDto userDto = UserFixture.createUserDto();

            given(userService.update(requestHeaderUserId, userId, userUpdateRequest))
                .willThrow(new UserNotFoundException(userId));

            // When & Then
            mockMvc.perform(patch("/api/users/{userId}", userId)
                    .requestAttr("userId", requestHeaderUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(userUpdateRequest)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("사용자 논리 삭제 테스트")
    class UserDeleteTests {

        @Test
        void 사용자를_논리_삭제시_204를_반환해야_한다() throws Exception {
            // Given
            UUID requestHeaderUserId = UserFixture.getDefaultId();
            UUID userId = UserFixture.getDefaultId();

            willDoNothing().given(userService).delete(requestHeaderUserId, userId);

            // When & Then
            mockMvc.perform(delete("/api/users/{userId}", userId)
                    .requestAttr("userId", requestHeaderUserId))
                .andExpect(status().isNoContent());
        }

        @Test
        void 다른_사용자를_논리_삭제_시_403을_반환해야_한다() throws Exception {
            // Given
            UUID requestHeaderUserId = UserFixture.getDefaultId();
            UUID userId = UUID.randomUUID();

            willThrow(new ForbiddenAccessException("다른 사용자는 삭제할 수 없습니다"))
                .given(userService)
                .delete(requestHeaderUserId, userId);

            // When & Then
            mockMvc.perform(delete("/api/users/{userId}", userId)
                    .requestAttr("userId", requestHeaderUserId))
                .andExpect(status().isForbidden());
        }

        @Test
        void 존재하지_않는_사용자를_논리_삭제_시_404를_반환해야_한다() throws Exception {
            // Given
            UUID requestHeaderUserId = UserFixture.getDefaultId();
            UUID userId = UUID.randomUUID();

            willThrow(new UserNotFoundException(userId))
                .given(userService)
                .delete(requestHeaderUserId, userId);

            // When & Then
            mockMvc.perform(delete("/api/users/{userId}", userId)
                    .requestAttr("userId", requestHeaderUserId))
                .andExpect(status().isNotFound());
        }

        @Test
        void 논리_삭제된_사용자를_논리_삭제_시_404를_반환해야_한다() throws Exception {
            // Given
            UUID requestHeaderUserId = UserFixture.getDefaultId();
            UUID userId = UUID.randomUUID();

            willThrow(new UserNotFoundException(userId))
                .given(userService)
                .delete(requestHeaderUserId, userId);

            // When & Then
            mockMvc.perform(delete("/api/users/{userId}", userId)
                    .requestAttr("userId", requestHeaderUserId))
                .andExpect(status().isNotFound());
        }
    }
}
