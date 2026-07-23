package com.sprint.mission.discodeit.dto.sse;

import java.time.Instant;
import java.util.UUID;

public record SseMessage(
    UUID id,
    String eventName,
    Object data,
    Instant createdAt
) {

  public static SseMessage of(String eventName, Object data) {
    return new SseMessage(UUID.randomUUID(), eventName, data, Instant.now());
  }
}