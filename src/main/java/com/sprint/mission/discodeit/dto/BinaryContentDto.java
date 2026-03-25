package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentDto(
        UUID id,
        String fileName,
        String contentType,
        byte[] bytes,
        Instant createdAt
) {
}