package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent {
    private UUID id;
    private Instant createdAt;
    private byte[] data;
    private String fileName;
    private Long size;
    private String contentType;

    public BinaryContent(byte[] data, String fileName,
                         Long size, String contentType) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.data = data;
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }
}
