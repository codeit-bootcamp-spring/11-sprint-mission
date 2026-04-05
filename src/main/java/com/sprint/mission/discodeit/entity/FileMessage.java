package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FileMessage {
    private UUID id;
    private UUID messageId;// 어떤 메시지에 첨부된 파일인지(외래키 역할)
    private UUID userId;    // 파일 소유자(추가)
    private String fileName;    // 원본 파일명
    private String contentType; // MIME 타임(ex: "image/png")
    private byte[] content;     // 바이너리 파일 데이터

    // 생성자
    public FileMessage() {
        this.id = UUID.randomUUID();
    }

    public FileMessage(UUID messageId, UUID userId, String fileName, String contentType, byte[] content) {
        this.id = UUID.randomUUID();
        this.messageId = messageId;
        this.userId = userId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.content = content;
    }
}
