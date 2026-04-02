package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentResponse(
    UUID id,
    String fileName,
    String contentType,
    long size,
    Instant createdAt,
    String bytes
) {

  public static BinaryContentResponse from(BinaryContent content) {
    return new BinaryContentResponse(
        content.getId(),
        content.getFileName(),
        content.getContentType(),
        content.getSize(),
        content.getCreatedAt(),
        content.getBytes()
    );
  }
}
