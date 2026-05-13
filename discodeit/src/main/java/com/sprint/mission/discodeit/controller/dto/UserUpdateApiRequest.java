package com.sprint.mission.discodeit.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateApiRequest(
        @Size(max = 50) String newUsername,
        @Email @Size(max = 100) String newEmail,
        String newPassword
) {
}
