package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 정보")
public record LoginRequest(

        @NotBlank(message = "이름은 필수로 기재해야 합니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수로 기재해야 합니다.")
        String password
) {
}
