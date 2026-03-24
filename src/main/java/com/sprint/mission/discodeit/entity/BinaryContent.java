package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable, Identifiable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final Instant createdAt;
    private final byte[] data;
    private final String fileName;
    private final String contentType;
    private final long size;

    public BinaryContent(BinaryContentCreateRequest binaryContentCreateRequest) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.data = binaryContentCreateRequest.data();
        this.fileName = binaryContentCreateRequest.fileName();
        this.contentType = binaryContentCreateRequest.contentType();
        this.size = binaryContentCreateRequest.size();
    }

    public BinaryContentResponse toResponse() {
        return new BinaryContentResponse(
                this.data,
                this.fileName,
                this.contentType,
                this.size
        );
    }
}
