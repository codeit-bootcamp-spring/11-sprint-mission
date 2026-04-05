package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class BinaryContent {
    private UUID id;
    private Instant createdAt;

    private byte[] content;
    private String contentType;
    private UUID userId;
    private UUID messageId;

    public BinaryContent(String image, byte[] content, String contentType, UUID userId, UUID messageId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.content = content;
        this.contentType = contentType;
        this.userId = userId;
        this.messageId = messageId;
    }
}
