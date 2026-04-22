package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreatePart(
        @NotBlank(message = "username은 필수 입력 사항입니다.")
        String username,

        @NotBlank(message = "email은 필수 입력 사항입니다.")
        @Email(message = "올바른 Email 형식이 아닙니다.")
        String email,

        @NotBlank(message = "password는 필수 입력 사항입니다.")
        String password
        ) {
}
