package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContent;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record BinaryContentDto(
    UUID id,
    String fileName,
    String contentType,
    long size,
    @Schema(type = "string", format = "byte")
    byte[] bytes
) {

  public static BinaryContentDto from(BinaryContent content) {
    return new BinaryContentDto(
        content.getId(),
        content.getFileName(),
        content.getContentType(),
        content.getSize(),
        content.getBytes()
    );
  }
}
