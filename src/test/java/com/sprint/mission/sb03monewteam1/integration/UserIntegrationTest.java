package com.sprint.mission.sb03monewteam1.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.config.TestEnvSetup;
import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserUpdateRequest;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserIntegration 테스트")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    static void setUp() {
        TestEnvSetup.loadEnvVariables();
    }

    @Test
    void 사용자_생성_시_Repository까지_반영되어야_한다() throws Exception {
        // Given
        UserRegisterRequest userRegisterRequest = UserFixture.createUserRegisterRequest();

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsBytes(userRegisterRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(UserFixture.getDefaultEmail()))
            .andExpect(jsonPath("$.nickname").value(UserFixture.getDefaultNickname()));

        User user = userRepository.findByEmail(userRegisterRequest.email()).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(userRegisterRequest.email());
    }

    @Test
    void 중복된_이메일로_회원가입_시_409를_반환한다() throws Exception {
        // Given
        UserRegisterRequest userRegisterRequest = UserFixture.createUserRegisterRequest();
        User existUser = UserFixture.createUser();

        userRepository.save(existUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsBytes(userRegisterRequest)))
            .andExpect(status().isConflict());

    }

    @Test
    void 알맞은_형식이_아닌_비밀번호로_회원가입_시_400을_반환한다() throws Exception {
        // Given
        UserRegisterRequest userRegisterRequest = UserFixture.createUserRegisterRequestWithInvalidPassword();

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsBytes(userRegisterRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 정상적인_로그인_시_사용자_정보가_반환되어야_한다() throws Exception {
        // Given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        UserLoginRequest userLoginRequest = UserFixture.createUserLoginRequest();

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(userLoginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.nickname").value("testUser"));
    }

    @Test
    void 존재하지_않는_이메일로_로그인_시_401을_반환해야_한다() throws Exception {
        // Given
        User user = User.builder()
            .email("correct@example.com")
            .nickname("testUser")
            .password("Password123!")
            .build();
        userRepository.save(user);

        UserLoginRequest loginRequest = UserLoginRequest.builder()
            .email("wrong@example.com")
            .password("Password123!")
            .build();

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 잘못된_비밀번호로_로그인_시_401을_반환해야_한다() throws Exception {
        // Given
        User user = User.builder()
            .email("test@example.com")
            .nickname("testUser")
            .password("correctPassword123!")
            .build();
        userRepository.save(user);

        UserLoginRequest loginRequest = UserLoginRequest.builder()
            .email("test@example.com")
            .password("wrongPassword123!")
            .build();

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 사용자_정보_수정에_성공_시_레포지터리까지_반영되어야_한다() throws Exception {
        // Given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        UUID requestHeaderUserId = savedUser.getId();
        UUID userId = savedUser.getId();

        userRepository.save(user);
        UserUpdateRequest userUpdateRequest = UserFixture.userUpdateRequest("newNickName");

        // When & Then
        mockMvc.perform(patch("/api/users/{userId}", userId)
                .requestAttr("userId", requestHeaderUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(userUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(UserFixture.getDefaultEmail()))
            .andExpect(jsonPath("$.nickname").value("newNickName"));
    }

    @Test
    void 타인의_정보를_수정하려_하면_403을_반환해야_한다() throws Exception {
        // Given
        UUID requestHeaderUserId = UserFixture.getDefaultId();
        UUID userId = UUID.randomUUID();
        UserUpdateRequest userUpdateRequest = UserFixture.userUpdateRequest("newNickname");

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

        // When & Then
        mockMvc.perform(patch("/api/users/{userId}", userId)
                .requestAttr("userId", requestHeaderUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(userUpdateRequest)))
            .andExpect(status().isNotFound());
    }


}
