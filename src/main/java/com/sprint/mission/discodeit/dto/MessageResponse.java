package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID authorId,
        UUID channelId,
        String content,
        List<UUID> attachmentIds,
        Instant createdAt,
        Instant updatedAt
) {
    public static MessageResponse of(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getAuthorId(),
                message.getChannelId(),
                message.getContent(),
                message.getAttachmentIds(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}
