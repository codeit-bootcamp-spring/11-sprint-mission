package com.sprint.mission.discodeit.dto.channel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        String name,
        String description,
        boolean isPrivate,
        Instant lastMessageAt,
        List<UUID> participants
) {}