package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;

public record BinaryContentResponse(
        UUID id,
        String fileName,
        String contentType,
        byte[] bytes
) {
    public static BinaryContentResponse from(BinaryContent content) {
        return new BinaryContentResponse(
                content.getId(),
                content.getFileName(),
                content.getContentType(),
                content.getData()
        );
    }
}
