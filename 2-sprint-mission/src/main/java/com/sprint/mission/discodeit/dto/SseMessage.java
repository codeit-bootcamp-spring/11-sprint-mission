package com.sprint.mission.discodeit.dto;

import java.util.Set;
import java.util.UUID;

public record SseMessage(
    UUID eventId,
    String eventName,
    Object data,
    Set<UUID> receiverIds
) {

  public static SseMessage of(String eventName, Object data, Set<UUID> receiverIds) {
    return new SseMessage(UUID.randomUUID(), eventName, data, Set.copyOf(receiverIds));
  }

  public static SseMessage broadcast(String eventName, Object data) {
    return new SseMessage(UUID.randomUUID(), eventName, data, Set.of());
  }

  public boolean isReceivable(UUID receiverId) {
    return receiverIds.isEmpty() || receiverIds.contains(receiverId);
  }
}