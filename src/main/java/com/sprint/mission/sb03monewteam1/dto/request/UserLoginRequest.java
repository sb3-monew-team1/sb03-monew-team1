package com.sprint.mission.sb03monewteam1.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UserLoginRequest(

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    String email,

    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,20}$",
        message = "비밀번호는 6자 이상 20자 이하이며, 최소 하나의 영문자, 숫자, 특수문자(@$!%*?&)를 포함해야 합니다"
    )
    String password
) {

}
