package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class UserStatus {
    private final UUID id;
    private final UUID userId;
    private final Instant createdAt;
    private Instant updatedAt;

    public boolean isOnline() {
        Instant now = Instant.now();
        Instant fiveMinuteAgo = now.minusSeconds(5 * 60);

        return updatedAt.isAfter(fiveMinuteAgo);
    }
}
