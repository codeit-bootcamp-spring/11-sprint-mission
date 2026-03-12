package com.sprint.mission.discodeit.entity.Domain;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {
    // 이미지, 파일등 바이너리 데이터 표현하는 모델, 각각 데이터 저장용
    // User, Message id 참조 필드 추가
    // BinaryContent가 누구의 프로필인지, 어떤 메시지의 첨부파일인지 알아야 함.
    private static final long serialVersionUID = 1L;
    private UUID id;      // 유효아이디
    private Instant createdAt;   // 파일이 업로드된 시간
    private UUID userId;        // 누구의 프로필인지
    private UUID messageId;     // 어떤 메세지의 첨부파일인지
    private String fileName;    // 파일 이름
    private byte[] content;     // 파일 데이터
    private String contentType; // 파일 형식

    public BinaryContent(String fileName, byte[] content, String contentType) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.fileName = fileName;
        this.content = content;
        this.contentType = contentType;
    }

    // 프로필 이미지용
    public static BinaryContent forProfile(UUID userId, String fileName, byte[] content, String contentType){
        BinaryContent binaryContent = new BinaryContent(fileName, content, contentType);
        binaryContent.userId = userId;
        binaryContent.messageId = null;
        return binaryContent;
    }

    // 메세지 첨부파일용
    public static BinaryContent forMessage(UUID messageId, String fileName, byte[] content, String contentType){
        BinaryContent binaryContent = new BinaryContent(fileName, content, contentType);
        binaryContent.userId = null;
        binaryContent.messageId = messageId;
        return binaryContent;
    }
}