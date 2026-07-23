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

  private static final int MAX_SIZE = 1000;

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  public UUID save(SseMessage message) {
    UUID id = message.id();
    messages.put(id, message);
    eventIdQueue.addLast(id);

    while (eventIdQueue.size() > MAX_SIZE) {
      UUID oldestId = eventIdQueue.pollFirst();
      if (oldestId != null) {
        messages.remove(oldestId);
      }
    }
    return id;
  }

  public List<SseMessage> findAllAfter(UUID lastEventId) {
    if (lastEventId == null) {
      return List.of();
    }

    List<UUID> ids = new ArrayList<>(eventIdQueue);
    int index = ids.indexOf(lastEventId);

    if (index == -1) {
      // lastEventId가 이미 만료(1000개 제한으로 밀려남)되어 유실 구간을 정확히 복원 불가.
      // 이 경우 빈 리스트를 반환하고, 호출부(SseService.connect)에서 필요시
      // "유실 발생" 처리를 하도록 위임한다.
      return List.of();
    }

    return ids.subList(index + 1, ids.size()).stream()
        .map(messages::get)
        .filter(msg -> msg != null)
        .toList();
  }
}