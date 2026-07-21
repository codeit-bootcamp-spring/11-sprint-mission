package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus;

import java.util.UUID;

public record BinaryContentDto(
        UUID id,
        String fileName,
        long size,
        String contentType,
        BinaryContentStatus status
) {
}
