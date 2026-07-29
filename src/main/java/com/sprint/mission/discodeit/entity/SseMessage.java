package com.sprint.mission.discodeit.entity;

import java.time.Instant;
import java.util.UUID;

public record SseMessage(
    UUID eventId,
    UUID receiverId,
    String eventName,
    Object data,
    Instant createdAt
) {

}
