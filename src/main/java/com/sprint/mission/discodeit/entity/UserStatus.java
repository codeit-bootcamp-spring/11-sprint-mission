package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends BaseEntity {
    private UUID userId;
    private Instant lastActiveAt;

    public UserStatus(UUID userId) {
        this.userId = userId;
        this.lastActiveAt = Instant.now();
    }

    public boolean isOnline() {
        return lastActiveAt.plusSeconds(300).isAfter(Instant.now());
    }

    public void update(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
        setUpdatedAt(Instant.now());
    }

}
