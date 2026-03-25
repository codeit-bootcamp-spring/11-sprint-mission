package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelDto(
        UUID id,
        String name,
        String description,
        String type,
        List<UUID> participantIds,
        Instant lastMessageAt,
        Instant createdAt,
        Instant updatedAt
) {
}
