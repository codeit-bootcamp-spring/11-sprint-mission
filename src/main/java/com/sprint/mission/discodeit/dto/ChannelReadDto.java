package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Channel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelReadDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    Channel.Type type,
    String name,
    String description,
    List<UUID> participantIds,
    Instant lastMessageAt
) {

}
