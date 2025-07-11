package com.sprint.mission.sb03monewteam1.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRegisterRequest(

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    String email,

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해야 합니다")
    String nickname,

    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,20}$",
        message = "비밀번호는 6자 이상 20자 이하이며, 최소 하나의 영문자, 숫자, 특수문자(@$!%*?&)를 포함해야 합니다"
    )
    String password
) {

}
