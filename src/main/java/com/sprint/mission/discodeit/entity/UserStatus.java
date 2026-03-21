package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.UserStatusResponse;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends BaseEntity {
    private final UUID userId;

    public UserStatus(UUID userId) {
        this.userId = userId;
    }

    public UserStatusResponse toResponse() {
        return new UserStatusResponse(
                getUpdatedAt(),
                getUpdatedAt().isAfter(Instant.now().minusSeconds(5 * 60))
        );
    }
}
