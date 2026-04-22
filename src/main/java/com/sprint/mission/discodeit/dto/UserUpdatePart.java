package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UserUpdatePart(
        @Pattern(regexp = "\\S+", message = "올바르지 않은 입력 형식입니다.")
        String newUsername,

        @Email(message = "올바르지 않은 Email형식입니다.")
        String newEmail,

        @Pattern(regexp = "\\S+", message = "올바르지 않은 입력 형식입니다.")
        String newPassword
) {
}
