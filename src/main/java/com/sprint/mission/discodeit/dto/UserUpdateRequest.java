package com.sprint.mission.discodeit.dto;

public record UserUpdateRequest(
        String name,
        String email,
        String password,
        byte[] profileImage
) {
}
