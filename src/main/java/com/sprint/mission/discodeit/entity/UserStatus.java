package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
public class UserStatus implements Serializable {

    private static final long SerialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;

    private UUID userId;

    public UserStatus(UUID userId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        this.userId = userId;
    }

    public void updateTime() {
        updatedAt = Instant.now();
    }

    public boolean isOnline() {
        Instant lastTime = this.updatedAt;
        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);

        return lastTime.isAfter(fiveMinutesAgo);
    }

    @Override
    public String toString() {
        return "UserStatus{" +
                "userId='" + userId + '\'' +
                ", updateTime='" + updatedAt + '\'' +
                ", isOnline='" + isOnline() + '\'' +
                "}";
    }
}
