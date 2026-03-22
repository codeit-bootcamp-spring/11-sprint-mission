package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable { // 수정불가능한 도메인 모델로, updatedAt를 정의 하지않으므로 BaseEntity 상속x

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Instant createdAt;

    private final byte[] bytes; // 실제 바이너리 데이터
    private final String fileName;
    private final String contentType; // ex) image/png

    private boolean isDeleted;

    // 생성자
    public BinaryContent(byte[] bytes, String fileName, String contentType) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.bytes = bytes;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }
}