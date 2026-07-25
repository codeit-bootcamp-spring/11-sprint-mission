package com.sprint.mission.discodeit.dto.sse;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record SseMessage(
    UUID id,
    Set<UUID> receiverIds,
    String eventName,
    Object data,
    Instant createdAt
) {

  public SseMessage {
    id = UUID.randomUUID();
    receiverIds = receiverIds == null ? null : Set.copyOf(receiverIds);
    createdAt = Instant.now();
  }

  public boolean isTargetedTo(UUID receiverId) {
    return this.receiverIds == null || this.receiverIds.isEmpty()
        || this.receiverIds.contains(receiverId);
  }
}