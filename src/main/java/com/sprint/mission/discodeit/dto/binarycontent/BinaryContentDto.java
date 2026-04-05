package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentDto(
        UUID id,
        Instant createdAt,
        String fileName,
        Long size,
        String contentType,
        byte[] bytes
) {
    public static BinaryContentDto from(BinaryContent binaryContent) {
        return new BinaryContentDto(
                binaryContent.getId(),
                binaryContent.getCreateAt(),
                binaryContent.getFileName(),
                binaryContent.getData() != null ? (long) binaryContent.getData().length : 0L,
                binaryContent.getContentType(),
                binaryContent.getData()
        );
    }
}