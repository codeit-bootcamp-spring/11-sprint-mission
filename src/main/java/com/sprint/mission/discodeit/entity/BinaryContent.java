package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BinaryContent implements Serializable {

  // 객체 직렬화
  private static final long serialVersionUID = 1L;

  // 필드
  private final UUID id; // User의 profileId이자 message의 attachmentIds
  private final Instant createdAt;
  //    private Instant updatedAt; // 수정 불가능한 모델이기 때문에 updateAt 필드 정의 X
  private final UUID userId; // 유저의 프로필 이미지
  private final UUID messageId; // 메세지에 담긴 첨부파일
  private final byte[] bytes; // 실제 저장할 바이너리 데이터
  private final String originalName; // 데이터의 이름
  private final String contentType; // 데이터의 타입(.png 등)

  // 생성자
  private BinaryContent(UUID userId, UUID messageId, byte[] bytes, String originalName,
      String contentType) {
    this.id = UUID.randomUUID();
    this.createdAt = Instant.now();
    this.userId = userId;
    this.messageId = messageId;
    this.bytes = bytes;
    this.originalName = originalName;
    this.contentType = contentType;
  }

  // 프로필 이미지(정적 팩토리 메서드)
  public static BinaryContent userProfileImage(UUID userId, byte[] bytes, String originalName,
      String contentType) {
    return new BinaryContent(userId, null, bytes, originalName, contentType);
  }

  // 메시지 첨부파일(정적 팩토리 메서드)
  public static BinaryContent messageAttachment(UUID messageId, byte[] bytes, String originalName,
      String contentType) {
    return new BinaryContent(null, messageId, bytes, originalName, contentType);
  }
}
