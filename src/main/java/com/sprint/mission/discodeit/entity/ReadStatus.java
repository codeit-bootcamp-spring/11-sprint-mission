package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.io.Serializable;
import java.util.UUID;
import java.time.Instant;

@Getter
public class ReadStatus implements Serializable {
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    private final UUID userId;
    private final UUID channelId;
    private Instant lastReadAt;

    public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.createdAt = now;
        this.updatedAt = now;
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadAt = lastReadAt;
    }

    public void update(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
        this.updatedAt = Instant.now();
    }
}
