package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class Message implements Serializable {
    private UUID id;
    private UUID authorId;
    private UUID channelId;
    private List<UUID> attachmentIds;
    private Instant createdAt;
    private Instant updatedAt;
    private String content;
    private static final long serialVersionUID = 1L;

    public Message(String content, UUID userId, UUID channelId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.content = content;
        this.authorId = userId;
        this.channelId = channelId;
    }

    public void update(String content) {
       this.content = content;
       this.updatedAt = Instant.now();
    }

    public void setAttachmentIds(List<UUID> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }
}
