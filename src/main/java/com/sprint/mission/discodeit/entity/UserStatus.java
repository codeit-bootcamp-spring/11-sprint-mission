package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends BaseEntity {
    private final UUID userId;
    private Instant lastSeenAt;

    public UserStatus(UUID userId, Instant lastSeenAt) {
        super();
        this.userId = userId;
        this.lastSeenAt = lastSeenAt;
    }

    public void updateLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
        touch();
    }

    public boolean isOnline() {
        return lastSeenAt != null &&
                !lastSeenAt.isBefore(Instant.now().minus(Duration.ofMinutes(5)));
    }
}
