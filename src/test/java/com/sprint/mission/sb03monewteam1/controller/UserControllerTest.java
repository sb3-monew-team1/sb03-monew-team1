package com.sprint.mission.sb03monewteam1.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.sb03monewteam1.exception.user.InvalidEmailOrPasswordException;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.service.UserService;
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
        void 로그인에_성공하면_201이_반환되어야_한다() throws Exception {
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
}
