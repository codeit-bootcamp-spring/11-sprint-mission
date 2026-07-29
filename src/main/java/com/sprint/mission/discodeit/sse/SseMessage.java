package com.sprint.mission.discodeit.sse;

import java.util.Set;
import java.util.UUID;

public record SseMessage(
        UUID id,
        Set<UUID> receiverIds,
        String name,
        Object data
) {
}
