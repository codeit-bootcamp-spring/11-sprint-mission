package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BinaryContent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;

    private String fileName;
    private String contentType;
    private long size;

    private transient byte[] data;

    public BinaryContent(byte[] data, String fileName, String contentType) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.data = data;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = data != null ? data.length : 0;
    }
}
