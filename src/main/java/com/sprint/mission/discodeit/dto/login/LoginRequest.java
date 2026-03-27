package com.sprint.mission.discodeit.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "이름은 필수로 기재해야 합니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수로 기재해야 합니다.")
        String password
) {
}
