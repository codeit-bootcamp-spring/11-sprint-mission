package com.sprint.mission.discodeit.dto.binarycontent;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentDto(
        UUID id,
        Instant createdAt,
        String fileName,
        long size,
        String contentType,

        @Schema(format="byte")
        String bytes
) {
}
