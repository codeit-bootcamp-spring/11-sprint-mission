package com.sprint.mission.discodeit.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "사용자 이름은 필수입니다.")
        @Size(min = 2, max = 20, message = "사용자 이름은 2자 이상 20자 이하로 입력해주세요.")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        String password
) {}