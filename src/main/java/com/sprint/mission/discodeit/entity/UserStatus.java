package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus implements Serializable {
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    private final UUID userId;
    private Instant lastActiveAt;

    public UserStatus(UUID userId, Instant lastActiveAt) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.createdAt = now;
        this.updatedAt = now;
        this.userId = userId;
        this.lastActiveAt = lastActiveAt;
    }

    public void update(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
        this.updatedAt = Instant.now();
    }

    public boolean isOnline() {
        return lastActiveAt != null &&
                lastActiveAt.isAfter(Instant.now().minus(Duration.ofMinutes(5)));
    }
}