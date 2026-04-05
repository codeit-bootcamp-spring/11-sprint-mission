package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus implements Serializable {

    private static final long SerialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;

    private UUID userId;
    private UUID channelId;
    private Instant lastReadAt;

    public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        this.userId = userId;
        this.channelId = channelId;
        this.lastReadAt = this.createdAt;
    }

    public void updateLastReadAt() {
        lastReadAt = Instant.now();
        updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "ReadStatus{" +
                "userId='" + userId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", lastReadAt=" + lastReadAt + '\'' +
                 ", updateTime='" + updatedAt + '\'' +
                "}";
    }
}
