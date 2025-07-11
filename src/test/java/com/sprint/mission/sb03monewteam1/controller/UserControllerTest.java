package com.sprint.mission.sb03monewteam1.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
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
    }

}
