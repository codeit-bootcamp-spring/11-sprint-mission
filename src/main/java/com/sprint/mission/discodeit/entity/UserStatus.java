package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID userId;
    private final Instant createdAt;
    private Instant updatedAt;

    public UserStatus(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void update() {
        this.updatedAt = Instant.now();
    }

    public boolean isOnline() {
        if(updatedAt == null) {
            return false;
        }

        Instant now = Instant.now();
        Instant fiveMinuteAgo = now.minusSeconds(5 * 60);

        return updatedAt.isAfter(fiveMinuteAgo);
    }
}
