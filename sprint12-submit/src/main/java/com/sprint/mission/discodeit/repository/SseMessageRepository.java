package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.data.SseMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  public SseMessage save(Collection<UUID> receiverIds, String eventName, Object data) {
    UUID eventId = UUID.randomUUID();
    SseMessage message = new SseMessage(eventId, receiverIds, eventName, data);

    eventIdQueue.add(eventId);
    messages.put(eventId, message);

    return message;
  }

  public List<SseMessage> findAllByEventIdAfter(UUID lastEventId) {
    List<SseMessage> result = new ArrayList<>();
    boolean found = false;

    for (UUID eventId : eventIdQueue) {
      if (found) {
        SseMessage message = messages.get(eventId);

        if (message != null) {
          result.add(message);
        }
      }

      if (eventId.equals(lastEventId)) {
        found = true;
      }
    }

    return result;
  }

  public void deleteOldMessages(int maxSize) {
    while (eventIdQueue.size() > maxSize) {
      UUID oldEventId = eventIdQueue.poll();

      if (oldEventId != null) {
        messages.remove(oldEventId);
      }
    }
  }
}