package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.security.authority.UserRole;

import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String email,
        BinaryContentDto profile,
        boolean online,
        UserRole role
) {
}
