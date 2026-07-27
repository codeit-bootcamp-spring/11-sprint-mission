package com.sprint.mission.discodeit.sse.repository;

import com.sprint.mission.discodeit.sse.dto.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  // 이벤트가 발행한 순서를 저장
  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();

  // Message를 찾기 위한 저장소
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  public void save(SseMessage message) {
    messages.put(message.id(), message);
    eventIdQueue.add(message.id());
  }

  // 클라이언트가 유실된 데이터를 복원
  // 이벤트ID가 각각 a b c d e 순서로 보내고 C 이후 접속 종료 등으로 d, e가 유실됐을 때 c를 찾아 이후 d, e를 복구
  public List<SseMessage> findAfter(UUID lastEventId) {
    List<SseMessage> sseMessages = new ArrayList<>();
    boolean found = false;

    for (UUID eventId : eventIdQueue) {
      // lastEventId 이후 유실된 SseMessage 복구
      if (found) {
        sseMessages.add(messages.get(eventId));
      }

      // lastEventId일때 true
      if (eventId.equals(lastEventId)) {
        found = true;
      }
    }

    return sseMessages;
  }
}
