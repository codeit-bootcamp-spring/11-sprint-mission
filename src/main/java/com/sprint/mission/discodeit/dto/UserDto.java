package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record UserDto(
        UUID id,
        UUID profileId,
        String name,
        String email,
        boolean isOnline,
        Instant lastActiveAt
) {
    public static UserDto from(User user, UserStatus status) {
        return new UserDto(
                user.getId(),
                user.getProfileId(),
                user.getUsername(),
                user.getEmail(),
                Optional.ofNullable(status).map(UserStatus::isOnline).orElse(false),
                Optional.ofNullable(status).map(UserStatus::getUpdatedAt).orElse(null)
        );
    }
}
