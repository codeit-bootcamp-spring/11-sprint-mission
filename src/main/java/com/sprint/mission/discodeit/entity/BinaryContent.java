package com.sprint.mission.discodeit.entity;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BinaryContent {

  private UUID id;
  private Instant createdAt;
  private String fileName;        // 추가
  private long size;              // 추가
  private byte[] content;
  private String contentType;
  private UUID userId;
  private UUID messageId;

  public BinaryContent(String fileName, byte[] content, String contentType, UUID userId,
      UUID messageId) {
    this.id = UUID.randomUUID();
    this.createdAt = Instant.now();
    this.fileName = fileName;
    this.size = content != null ? content.length : 0;  // 추가
    this.content = content;
    this.contentType = contentType;
    this.userId = userId;
    this.messageId = messageId;
  }
}