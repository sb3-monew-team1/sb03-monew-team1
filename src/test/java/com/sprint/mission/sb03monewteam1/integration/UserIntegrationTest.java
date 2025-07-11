package com.sprint.mission.sb03monewteam1.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

        User user = userRepository.findByEmail(userRegisterRequest.email());
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

}
