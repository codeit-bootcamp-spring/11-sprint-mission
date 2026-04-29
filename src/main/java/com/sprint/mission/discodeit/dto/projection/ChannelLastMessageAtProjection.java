package com.sprint.mission.discodeit.dto.projection;

import java.time.Instant;
import java.util.UUID;

public record ChannelLastMessageAtProjection(
        UUID channelId,
        Instant lastMessageAt
) {
}
