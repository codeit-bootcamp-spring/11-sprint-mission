package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username은 필수 입력 사항입니다.")
        String username,

        @NotBlank(message = "password는 필수 입력 사항입니다.")
        String password
) {
}