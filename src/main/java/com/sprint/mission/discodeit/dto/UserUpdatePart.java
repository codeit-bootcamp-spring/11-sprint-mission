package com.sprint.mission.discodeit.dto;

public record UserUpdatePart(
        String newUsername,
        String newEmail,
        String newPassword
) {
}
