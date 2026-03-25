package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageDto(
        UUID id,
        UUID userId,
        UUID channelId,
        String content,
        List<UUID> attachmentIds,
        Instant createdAt,
        Instant updatedAt
) {
}