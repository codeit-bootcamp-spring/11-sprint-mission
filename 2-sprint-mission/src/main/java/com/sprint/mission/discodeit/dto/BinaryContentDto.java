package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.BinaryContent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public class BinaryContentDto {

    @Builder
    public record CreateRequest(
            @NotBlank(message = "파일명은 필수 항목입니다.")
            String fileName,

            @NotNull(message = "파일 크기 정보가 누락되었습니다.")
            Long size,

            @NotBlank(message = "콘텐츠 타입은 필수 항목입니다.")
            String contentType,

            @NotEmpty(message = "파일 데이터가 비어있습니다.")
            byte[] bytes
    ) {
        // DTO -> Entity
        public BinaryContent toEntity() {
            return BinaryContent.builder()
                    .id(UUID.randomUUID())
                    .createdAt(Instant.now())
                    .fileName(this.fileName)
                    .size((long) this.bytes.length)
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