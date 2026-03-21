package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final Instant createdAt;
    private final byte[] data;
    private final String fileName;
    private final String contentType;
    private final long size;

    public BinaryContent(UUID id, Instant createdAt, byte[] data, String fileName, String contentType, long size) {
        this.id = id;
        this.createdAt = createdAt;
        this.data = data;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
    }
}
