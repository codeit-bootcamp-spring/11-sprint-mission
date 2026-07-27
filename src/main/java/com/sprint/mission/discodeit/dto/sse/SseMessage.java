package com.sprint.mission.discodeit.dto.sse;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record SseMessage(
    UUID id,
    String eventName,
    Object data,
    Set<UUID> receiverIds,
    Instant createdAt
) {

  public boolean isTargetOf(UUID receiverId) {
    return receiverIds == null || receiverIds.contains(receiverId);
  }
}
