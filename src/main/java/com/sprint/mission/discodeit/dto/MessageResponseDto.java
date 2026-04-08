package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponseDto(
    UUID id,
    UUID channelId,
    UUID authorId,
    String content,
    Instant createdAt,
    Instant updatedAt,
    List<UUID> attachmentIds
) {

  public static MessageResponseDto from(Message message) {
    return new MessageResponseDto(
        message.getId(),
        message.getChannelId(),
        message.getAuthorId(),
        message.getMessage(),
        message.getCreatedAt(),
        message.getUpdatedAt(),
        message.getAttachmentIds()
    );
  }
}
