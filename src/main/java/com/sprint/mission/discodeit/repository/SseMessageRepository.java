package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();

  private final Map<UUID, SseMessage> sseMessageMap = new ConcurrentHashMap<>();

  private static final int MAX_CACHE_SIZE = 1000;

  public UUID save(SseMessage message) {
    UUID eventId = UUID.randomUUID();
    SseMessage savedMessage = new SseMessage(eventId, message.eventName(),
        message.data());

    sseMessageMap.put(eventId, savedMessage);
    eventIdQueue.addLast(eventId);

    if (eventIdQueue.size() > MAX_CACHE_SIZE) {
      UUID oldestId = eventIdQueue.pollFirst();
      sseMessageMap.remove(oldestId);
    }
    return eventId;
  }

  public List<SseMessage> findAllAfter(UUID lastEventId) {
    List<SseMessage> missedMessages = new ArrayList<>();
    boolean isAfterLastEvent = false;

    for (UUID id : eventIdQueue) {
      if (isAfterLastEvent) {
        missedMessages.add(sseMessageMap.get(id));
      }
      if (id.equals(lastEventId)) {
        isAfterLastEvent = true;
      }
    }
    return missedMessages;
  }
}
