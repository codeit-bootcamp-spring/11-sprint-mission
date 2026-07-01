package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;

public record BinaryContentResponseDTO(
        UUID id,
        String fileName,
        String contentType,
        byte[] data
) {
    public static BinaryContentResponseDTO from(BinaryContent binaryContent) {
        return new BinaryContentResponseDTO(
                binaryContent.getId(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getData()
        );
    }
}