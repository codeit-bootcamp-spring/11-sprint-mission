package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String content,
        UUID channelId,
        UUID authorId, // API 명세서의 authorId에 맞추기 위해 변환
        List<UUID> attachmentIds
) {
    public static MessageDto from(Message message) {
        return new MessageDto(
                message.getId(),
                message.getCreateAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getChannelId(),
                message.getUserId(), // Entity의 userId -> DTO의 authorId 매핑
                message.getAttachmentIds()
        );
    }
}