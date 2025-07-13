package com.sprint.mission.sb03monewteam1.controller.api;

import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자 관리", description = "사용자 관련 API")
public interface UserApi {

    @Operation(summary = "회원가입")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이메일 중복",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<UserDto> create(
        UserRegisterRequest userRegisterRequest
    );

    @Operation(summary = "로그인")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "로그인 실패 (이메일 또는 비밀번호 불일치)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<UserDto> login(
        UserLoginRequest userLoginRequest
    );
}
