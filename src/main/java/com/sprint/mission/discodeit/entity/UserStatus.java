package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.baseentity.UpdatableEntity;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends UpdatableEntity {
    private UUID userId;

    public UserStatus(UUID userId) {
        super();
        this.userId = userId;
    }

    public boolean passed() {
        return Duration.between(getUpdatedAt(), Instant.now()).toMinutes() <= 5;
    }
}
