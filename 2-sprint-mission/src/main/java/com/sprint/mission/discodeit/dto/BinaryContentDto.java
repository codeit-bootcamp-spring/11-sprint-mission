package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.BinaryContent;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public class BinaryContentDto {

    public record CreateRequest(
            String fileName,
            Long size,
            String contentType,
            byte[] bytes
    ) {
        // DTO -> Entity
        public BinaryContent toEntity() {
            return BinaryContent.builder()
                    .id(UUID.randomUUID())
                    .createdAt(Instant.now())
                    .fileName(this.fileName)
                    .size(this.size)
                    .contentType(this.contentType)
                    .bytes(this.bytes)
                    .build();
        }
    }

    @Builder
    public record Response(
            UUID id,
            String fileName,
            Long size,
            String contentType,
            byte[] bytes,
            Instant createdAt
    ) {
        // Entity -> DTO
        public static Response of(BinaryContent content) {
            return Response.builder()
                    .id(content.getId())
                    .fileName(content.getFileName())
                    .size(content.getSize())
                    .contentType(content.getContentType())
                    .bytes(content.getBytes())
                    .createdAt(content.getCreatedAt())
                    .build();
        }
    }
}