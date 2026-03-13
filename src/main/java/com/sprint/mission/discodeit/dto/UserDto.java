package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        UUID profileId,
        String name,
        String email,
        String password,
        boolean isOnline,
        Instant lastActiveAt
) {
    public static UserDto from(User user, UserStatus status) {
        return new UserDto(
                user.getId(),
                user.getProfileId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                status.isOnline(),
                status.getUpdatedAt()
        );
    }
}
