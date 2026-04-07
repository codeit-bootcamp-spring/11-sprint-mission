package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent {

  private UUID id;
  private Instant createdAt;
  private UUID userId;
  private UUID messageId;
  private String fileName;
  private byte[] content;
  private String contentType;

  public BinaryContent(String fileName, byte[] content, String contentType) {
    this.id = UUID.randomUUID();
    this.createdAt = Instant.now();
    this.fileName = fileName;
    this.content = content;
    this.contentType = contentType;
  }

  public void validateService() {
    if (this.fileName == null || this.fileName.isBlank()) {
      throw DiscodeitInvalidInputException.blankField("fileName");
    }
    if (this.content == null || this.content.length == 0) {
      throw DiscodeitInvalidInputException.blankField("content");
    }
    if (this.contentType == null || this.contentType.isBlank()) {
      throw DiscodeitInvalidInputException.blankField("contentType");
    }
    if (this.userId == null && this.messageId == null) {
      throw DiscodeitInvalidInputException.blankField("userId or messageId");
    }
  }

  // 프로필 이미지
  public static BinaryContent forProfile(UUID userId, String fileName, byte[] content,
      String contentType) {
    BinaryContent binaryContent = new BinaryContent(fileName, content, contentType);
    binaryContent.userId = userId;
    binaryContent.messageId = null;
    return binaryContent;
  }

  // 메세지 첨부파일
  public static BinaryContent forMessage(UUID messageId, String fileName, byte[] content,
      String contentType) {
    BinaryContent binaryContent = new BinaryContent(fileName, content, contentType);
    binaryContent.userId = null;
    binaryContent.messageId = messageId;
    return binaryContent;
  }
}