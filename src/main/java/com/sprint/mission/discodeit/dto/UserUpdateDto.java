package com.sprint.mission.discodeit.dto;

public record UserUpdateDto(
        String newName,
        String newEmail,
        String newPassword,
        byte[] bytes,
        String fileName,
        String fileType
) {
}
