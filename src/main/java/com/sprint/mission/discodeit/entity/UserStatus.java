package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends Common{
    private UUID userId;

    public UserStatus(UUID userId) {
        super();
        this.userId = userId;
    }

    public boolean passed() {
        return Duration.between(getUpdatedAt(), Instant.now()).toMinutes() <= 5;
    }
}
