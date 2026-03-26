package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentResponse (
        UUID id,
        String fileName,
        String contentType,
        byte[] bytes,
        Instant createdAt
){
    public static BinaryContentResponse of(BinaryContent binaryContent){
        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getBytes(),
                binaryContent.getCreatedAt()
        );
    }
}
