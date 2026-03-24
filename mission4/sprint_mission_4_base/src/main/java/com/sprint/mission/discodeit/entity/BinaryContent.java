package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final UUID messageId; // 메시지와의 연관관계 추가
    private final Instant createdAt;
    private final String fileName;
    private final Long size;
    private final String contentType;
    private final byte[] bytes;

    // 불변 도메인: 생성 시 모든 필드를 초기화하며, updatedAt은 포함하지 않음
    public BinaryContent(UUID messageId, String fileName, Long size, String contentType, byte[] bytes) {
        this.id = UUID.randomUUID();
        this.messageId = messageId;
        this.createdAt = Instant.now();
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.bytes = bytes;
    }
}