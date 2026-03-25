package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record BinaryContentCreateDto(
        UUID userId,
        UUID messageId,
        byte[] bytes,
        String fileName,
        String fileType
) {
}
