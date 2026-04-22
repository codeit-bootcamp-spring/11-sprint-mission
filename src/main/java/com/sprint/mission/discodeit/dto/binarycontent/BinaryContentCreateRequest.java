package com.sprint.mission.discodeit.dto.binarycontent;

import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

public record BinaryContentCreateRequest(
    String fileName,
    String contentType,
    byte[] bytes
) {

  // 정적 팩토리 메서드 추가
  public static Optional<BinaryContentCreateRequest> resolveAttachmentRequest(
      MultipartFile attachment) {
    if (attachment == null || attachment.isEmpty()) {
      return Optional.empty();
    } else {
      try {
        BinaryContentCreateRequest dto = new BinaryContentCreateRequest(
            attachment.getOriginalFilename(),
            attachment.getContentType(),
            attachment.getBytes() // 예외처리 필수
        );
        return Optional.of(dto);
      } catch (IOException e) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Failed to process the uploaded file", e);
      }
    }
  }
}
