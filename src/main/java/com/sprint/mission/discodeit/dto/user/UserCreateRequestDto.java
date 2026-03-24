package com.sprint.mission.discodeit.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserCreateRequestDto(

        @NotBlank(message = "이름은 필수로 기재해야 합니다.")
        String name,

        @NotBlank(message = "이메일은 필수로 기재해야 합니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수로 기재해야 합니다.")
        @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하여야 합니다.")
        String password,

        UUID profileId
) {
}
