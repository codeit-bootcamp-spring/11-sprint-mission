package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends BaseEntity {
    private final UUID userId;

    public UserStatus(User user) {
        this.userId = user.getId();
    }

    public UserStatusResponse toResponse() {
        return new UserStatusResponse(
                getUpdatedAt(),
                getUpdatedAt().isAfter(Instant.now().minusSeconds(5 * 60))
        );
    }
}
