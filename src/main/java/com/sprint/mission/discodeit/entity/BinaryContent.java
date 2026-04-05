package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {

    private static final long SerialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;

    // 파일 이름 필드 추가 (크기, 종류)
    private byte[] bytes;

    public BinaryContent(byte[] image) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();

        this.bytes = image;
    }
}
