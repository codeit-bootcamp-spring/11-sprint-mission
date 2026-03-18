package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message {

    private final UUID id;
    private final long createdAt;
    private long updatedAt;

    private UUID userId;
    private UUID channelId;
    private String content;

    public Message(UUID userId, UUID channelId, String content) {
        this.id = UUID.randomUUID();
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
        this.userId = userId;
        this.channelId = channelId;
        this.content = content;
    }

    // === getter 메서드들 ===
    public UUID getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public String getContent() {
        return content;
    }

    // === update 메서드 ===
    public void update(UUID userId, UUID channelId, String content) {
        this.userId = userId;
        this.channelId = channelId;
        this.content = content;
        this.updatedAt = System.currentTimeMillis();
    }
}
