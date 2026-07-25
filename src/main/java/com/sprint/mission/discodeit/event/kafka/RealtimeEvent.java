package com.sprint.mission.discodeit.event.kafka;

import java.util.List;
import java.util.UUID;

public record RealtimeEvent(
        String type,
        String eventName,
        List<UUID> receiverIds,
        Object data
) {
}
