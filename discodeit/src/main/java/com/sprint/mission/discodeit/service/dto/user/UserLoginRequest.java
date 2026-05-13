package com.sprint.mission.discodeit.service.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(
        @NotBlank String username,
        @NotNull String password
) {
}
