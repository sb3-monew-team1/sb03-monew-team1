package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserUpdateRequest;
import com.sprint.mission.sb03monewteam1.entity.User;
import java.time.Instant;
import java.util.UUID;

public class UserFixture {

    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String DEFAULT_NICKNAME = "testUser";
    private static final String DEFAULT_PASSWORD = "!password123";
    private static final UUID DEFAULT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    // UserRegisterRequest 생성
    public static UserRegisterRequest createUserRegisterRequest() {
        return UserRegisterRequest.builder()
            .email(DEFAULT_EMAIL)
            .nickname(DEFAULT_NICKNAME)
            .password(DEFAULT_PASSWORD)
            .build();
    }

    public static UserRegisterRequest createUserRegisterRequest(String email, String nickname,
        String password) {
        return UserRegisterRequest.builder()
            .email(email)
            .nickname(nickname)
            .password(password)
            .build();
    }

    // UserLoginRequest 생성
    public static UserLoginRequest createUserLoginRequest() {
        return UserLoginRequest.builder()
            .email(DEFAULT_EMAIL)
            .password(DEFAULT_PASSWORD)
            .build();
    }

    public static UserLoginRequest createUserLoginRequest(String email, String password) {
        return UserLoginRequest.builder()
            .email(email)
            .password(password)
            .build();
    }

    // UserUpdateRequest
    public static UserUpdateRequest userUpdateRequest() {
        return UserUpdateRequest.builder()
            .nickname(DEFAULT_NICKNAME)
            .build();
    }

    public static UserUpdateRequest userUpdateRequest(String nickname) {
        return UserUpdateRequest.builder()
            .nickname(nickname)
            .build();
    }

    // User 엔티티 생성
    public static User createUser() {
        return User.builder()
            .email(DEFAULT_EMAIL)
            .nickname(DEFAULT_NICKNAME)
            .password(DEFAULT_PASSWORD)
            .build();
    }

    public static User createUser(String email, String nickname, String password) {
        return User.builder()
            .email(email)
            .nickname(nickname)
            .password(password)
            .build();
    }

    // UserDto 생성
    public static UserDto createUserDto() {
        return UserDto.builder()
            .id(DEFAULT_ID)
            .email(DEFAULT_EMAIL)
            .nickname(DEFAULT_NICKNAME)
            .createdAt(Instant.now())
            .build();
    }

    public static UserDto createUserDto(UUID id, String email, String nickname, Instant createdAt) {
        return UserDto.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .createdAt(createdAt)
            .build();
    }

    // 중복 이메일 테스트용 팩토리 메서드
    public static UserRegisterRequest createUserRegisterRequestWithDuplicateEmail() {
        return UserRegisterRequest.builder()
            .email("duplicate@example.com")
            .nickname("duplicateUser")
            .password("!password123")
            .build();
    }

    // 무효한 이메일 테스트용 팩토리 메서드
    public static UserRegisterRequest createUserRegisterRequestWithInvalidEmail() {
        return UserRegisterRequest.builder()
            .email("invalid-email")
            .nickname("testUser")
            .password("!password123")
            .build();
    }

    // 빈 닉네임 테스트용 팩토리 메서드
    public static UserRegisterRequest createUserRegisterRequestWithBlankNickname() {
        return UserRegisterRequest.builder()
            .email("test@example.com")
            .nickname("")
            .password("!password123")
            .build();
    }

    // 무효한 패스워드 테스트용 팩토리 메서드
    public static UserRegisterRequest createUserRegisterRequestWithInvalidPassword() {
        return UserRegisterRequest.builder()
            .email("test@example.com")
            .nickname("testUser")
            .password("invalid")
            .build();
    }

    // 기본 상수 접근자 메서드
    public static String getDefaultEmail() {
        return DEFAULT_EMAIL;
    }

    public static String getDefaultNickname() {
        return DEFAULT_NICKNAME;
    }

    public static UUID getDefaultId() {
        return DEFAULT_ID;
    }
}