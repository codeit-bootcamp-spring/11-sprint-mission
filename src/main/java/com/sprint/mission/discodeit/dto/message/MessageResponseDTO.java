package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public record MessageResponseDTO(
        UUID messageId,
        String content,
        UUID channelId,
        UUID userId,
        List<UUID> attachmentIds
) {
    public static MessageResponseDTO from(Message message) {
        return new MessageResponseDTO(
                message.getId(),
                message.getContent(),
                message.getChannelId(),
                message.getUserId(),
                message.getAttachmentIds()
        );
    }
}
