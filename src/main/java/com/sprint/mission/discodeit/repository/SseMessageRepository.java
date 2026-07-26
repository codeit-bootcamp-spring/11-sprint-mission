package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private static final int MAX_SIZE = 1_000;

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();
  private final AtomicInteger size = new AtomicInteger();

  public UUID save(SseMessage message) {
    this.messages.put(message.id(), message);
    this.eventIdQueue.addLast(message.id());

    if (this.size.incrementAndGet() > MAX_SIZE) {
      UUID evictedId = this.eventIdQueue.pollFirst();
      if (evictedId != null) {
        this.messages.remove(evictedId);
        this.size.decrementAndGet();
      }
    }

    return message.id();
  }

  public List<SseMessage> findAllAfter(UUID lastEventId, UUID receiverId) {
    if (lastEventId == null) {
      return List.of();
    }

    // 단일 인스턴스 전제: lastEventId가 없으면(캡 초과로 밀려났거나 재시작됨) 큐 전체가
    // 항상 lastEventId보다 나중이라고 가정하고 전부 재생한다. 인스턴스가 여러 대면
    // 각자 로컬 큐라 이 가정이 깨진다 (Kafka 리팩토링 대상).
    boolean found = this.messages.containsKey(lastEventId);
    Stream<UUID> ids = this.eventIdQueue.stream();
    if (found) {
      ids = ids.dropWhile(id -> !id.equals(lastEventId)).skip(1);
    }

    return ids
        .map(this.messages::get)
        .filter(Objects::nonNull)
        .filter(message -> message.isTargetedTo(receiverId))
        .toList();
  }

  public UUID getLatestEventId() {
    return this.eventIdQueue.peekLast();
  }
}