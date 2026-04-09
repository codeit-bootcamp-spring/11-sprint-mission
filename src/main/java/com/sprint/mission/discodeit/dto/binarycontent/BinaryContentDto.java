package com.sprint.mission.discodeit.dto.binarycontent;

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

}
