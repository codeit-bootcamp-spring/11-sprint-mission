package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.UUID;

public record MessageResponseDto(
        UUID id,
        UUID channelId,
        UUID userId,
        String message,
        Instant createdAt
) {
    public static MessageResponseDto from(Message message) {
        return new MessageResponseDto(
                message.getId(),
                message.getChannelId(),
                message.getAuthorId(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }
}
