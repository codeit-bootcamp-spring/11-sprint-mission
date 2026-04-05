package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.BinaryContent;
import java.util.UUID;

public record BinaryContentCreateRequest(
    UUID userId,
    UUID messageId,
    byte[] bytes,
    String originalName,
    String contentType
) {

  // userId가 없을 경우 첨부파일을, 있을 경우 프로필 이미지를 binaryContent로 설정
  public BinaryContent toBinaryContent() {
    if (userId != null) {
      return BinaryContent.userProfileImage(userId, bytes, originalName, contentType);
    }
    return BinaryContent.messageAttachment(messageId, bytes, originalName, contentType);
  }
}
// 원래라면 @JsonProperty를 매개변수 앞에 붙이고, @JsonCreator을 사용하여 json에서 자바객체로 역직렬화 시 생성자가 있어야 했으나
// JDK 14 이후 Jackson이 record를 인식하고 자동으로 처리를 해주기 때문에 @JsonCreator, @JsonProperty 생략이 가능해졌다.