package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.SseMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private static final int MAX_SIZE = 1000;

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  public UUID save(UUID receiverId, String eventName, Object data) {
    UUID eventId = UUID.randomUUID();
    SseMessage message = new SseMessage(eventId, receiverId, eventName, data, Instant.now());

    messages.put(eventId, message);
    eventIdQueue.addLast(eventId);

    if (eventIdQueue.size() > MAX_SIZE) {
      UUID oldest = eventIdQueue.pollFirst();
      messages.remove(oldest);
    }
    return eventId;
  }

  public List<SseMessage> findAllAfter(UUID lastEventId) {
    boolean found = false;
    List<SseMessage> result = new ArrayList<>();
    for (UUID id : eventIdQueue) {
      if (found) {
        result.add(messages.get(id));
      }
      if (id.equals(lastEventId)) {
        found = true;
      }
    }

    if (!found) {
      // 큐 끝까지 돌았는데도 못찾음 -> 복원 불가능
      return null;
    }

    return result;
  }

  public boolean isRestorable(UUID lastEventId) {
    return eventIdQueue.contains(lastEventId);
  }

}
