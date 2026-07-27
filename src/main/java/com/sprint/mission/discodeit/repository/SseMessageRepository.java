package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private static final int MAX_SIZE = 1000;

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  public UUID save(SseMessage message) {
    UUID eventId = UUID.randomUUID();
    SseMessage saved = new SseMessage(
        eventId, message.eventName(), message.data(), message.receiverIds(), message.createdAt()
    );

    messages.put(eventId, saved);
    eventIdQueue.addLast(eventId);

    while (eventIdQueue.size() > MAX_SIZE) {
      UUID oldestId = eventIdQueue.pollFirst();
      if (oldestId != null) {
        messages.remove(oldestId);
      }
    }

    return eventId;
  }

  public List<SseMessage> findAllAfter(UUID lastEventId) {
    if (lastEventId == null) {
      return List.of();
    }
    List<UUID> ids = new ArrayList<>(eventIdQueue);
    int index = ids.indexOf(lastEventId);
    if (index == -1) {
      return List.of();
    }
    return ids.subList(index + 1, ids.size()).stream()
        .map(messages::get)
        .filter(Objects::nonNull)
        .toList();
  }
}
