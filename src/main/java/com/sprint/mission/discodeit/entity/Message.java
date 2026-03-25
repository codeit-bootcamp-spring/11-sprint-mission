package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import java.time.Instant;
import java.util.ArrayList;

@Getter
public class Message implements Serializable{
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    private UUID userId;
    private UUID channelId;
    private String content;

    private List<UUID> attachmentIds;

    public Message(UUID userId, UUID channelId, String content) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.createdAt = now;
        this.updatedAt = now;
        this.userId = userId;
        this.channelId = channelId;
        this.content = content;
        this.attachmentIds = new ArrayList<>();
    }

    public void update(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public void updateAttachments(List<UUID> attachmentIds) {
        this.attachmentIds = new ArrayList<>(attachmentIds);
        this.updatedAt = Instant.now();
    }
}
