package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class SseMessageRepository {

  private static final int MAX_SIZE = 1000;

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();
  private final AtomicInteger size = new AtomicInteger();

  public UUID save(SseMessage message) {
    UUID eventId = message.eventId();
    messages.put(eventId, message);
    eventIdQueue.addLast(eventId);

    if (size.incrementAndGet() > MAX_SIZE) {
      UUID oldest = eventIdQueue.pollFirst();
      if (oldest != null) {
        messages.remove(oldest);
        size.decrementAndGet();
      }
    }
    return eventId;
  }

  public List<SseMessage> findAllAfter(UUID lastEventId) {
    List<SseMessage> missed = new ArrayList<>();
    boolean found = false;

    for (UUID eventId : eventIdQueue) {
      if (found) {
        SseMessage message = messages.get(eventId);
        if (message != null) {
          missed.add(message);
        }
      } else if (eventId.equals(lastEventId)) {
        found = true;
      }
    }

    if (!found) {
      log.warn("복원 범위 초과로 복원 불가한 lastEventId: {}", lastEventId);
      return List.of();
    }
    return missed;
  }
}