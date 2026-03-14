package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        String name,
        ChannelType type,
        String description,
        UUID ownerId,
        Instant lastMessageAt,
        List<UUID> memberIds
) {
}
