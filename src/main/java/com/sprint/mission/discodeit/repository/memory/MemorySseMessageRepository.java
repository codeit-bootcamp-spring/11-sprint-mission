package com.sprint.mission.discodeit.repository.memory;

import com.sprint.mission.discodeit.dto.SseMessage;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class MemorySseMessageRepository implements SseMessageRepository {

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();
  private static final int MAX_CACHE_SIZE = 1000;

  @Override
  public void save(UUID eventId, SseMessage message) {
    messages.put(eventId, message);
    eventIdQueue.addLast(eventId);

    if (eventIdQueue.size() > MAX_CACHE_SIZE) {
      UUID oldEventId = eventIdQueue.pollFirst();
      if (oldEventId != null) {
        messages.remove(oldEventId);
      }
    }
  }

  @Override
  public List<SseMessage> findEventsAfter(UUID lastEventId) {
    List<SseMessage> missingEvents = new ArrayList<>();
    boolean found = false;

    for (UUID eventId : eventIdQueue) {
      if (found) {
        missingEvents.add(messages.get(eventId));
      }
      if (eventId.equals(lastEventId)) {
        found = true;
      }
    }
    return missingEvents;
  }
}