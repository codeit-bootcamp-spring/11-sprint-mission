package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent {
    private UUID id;
    private Instant createdAt;
    private byte[] data;

    public BinaryContent(byte[] data) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.data = data;
    }
}
