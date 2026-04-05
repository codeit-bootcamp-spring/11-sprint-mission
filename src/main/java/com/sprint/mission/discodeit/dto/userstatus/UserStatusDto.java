package com.sprint.mission.discodeit.dto.userstatus;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserStatusDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID userId,
        Instant lastActiveAt,
        boolean online
) {
    public static UserStatusDto from(UserStatus userStatus) {
        boolean isOnline = "ONLINE".equals(userStatus.calculateCurrentStatus());
        return new UserStatusDto(
                userStatus.getId(),
                userStatus.getCreateAt(),
                userStatus.getUpdatedAt(),
                userStatus.getUserId(),
                userStatus.getLastOnlineTime(),
                isOnline
        );
    }
}