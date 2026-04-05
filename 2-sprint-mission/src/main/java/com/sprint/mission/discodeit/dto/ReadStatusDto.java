package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.ReadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public class ReadStatusDto {

    public record CreateRequest(
            @NotNull(message = "유저 ID는 필수 항목입니다.")
            UUID userId,

            @NotNull(message = "채널 ID는 필수 항목입니다.")
            UUID channelId
    ) {
        // DTO -> Entity
        public ReadStatus toEntity() {
            return ReadStatus.builder()
                    .userId(this.userId)
                    .channelId(this.channelId)
                    .lastReadAt(Instant.now())
                    .build();
        }
    }

    public record UpdateRequest(
    ) {}

    @Builder
    public record Response(
            UUID id,
            UUID userId,
            UUID channelId,
            Instant lastReadAt
    ) {
        // Entity -> DTO
        public static Response of(ReadStatus readStatus) {
            return Response.builder()
                    .id(readStatus.getId())
                    .userId(readStatus.getUserId())
                    .channelId(readStatus.getChannelId())
                    .lastReadAt(readStatus.getLastReadAt())
                    .build();
        }
    }
}