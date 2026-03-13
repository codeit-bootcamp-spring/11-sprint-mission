package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponseDto(
        UUID id,
        ChannelType channelType,
        String name,
        String description,
        Instant latestMessageCreatedAt,
        List<UUID> userIds
) {
}
