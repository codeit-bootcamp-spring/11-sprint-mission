package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BinaryContent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;

    private byte[] data;

    public BinaryContent(byte[] data) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.data = data;
    }
}
