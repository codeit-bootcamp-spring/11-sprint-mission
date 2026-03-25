package com.sprint.mission.discodeit.dto.user;

public record UserCreateDto(
        String name,
        String email,
        String password,
        byte[] profileImage
) {
}


