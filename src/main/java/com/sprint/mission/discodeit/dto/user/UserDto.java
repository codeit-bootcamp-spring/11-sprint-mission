package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String username,
        String email,
        UUID profileId,
        boolean online
) {
    public static UserDto from(User user, boolean online) {
        return new UserDto(
                user.getId(),
                user.getCreateAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                online
        );
    }
}