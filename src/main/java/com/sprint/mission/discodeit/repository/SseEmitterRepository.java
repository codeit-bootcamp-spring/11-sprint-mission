package com.sprint.mission.discodeit.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

  private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

  public void save(UUID receiverId, SseEmitter emitter) {
    this.data.computeIfAbsent(receiverId, id -> new CopyOnWriteArrayList<>()).add(emitter);
  }

  public List<SseEmitter> findAllByReceiverId(UUID receiverId) {
    return this.data.getOrDefault(receiverId, List.of());
  }

  public void remove(UUID receiverId, SseEmitter emitter) {
    this.data.computeIfPresent(receiverId, (id, emitters) -> {
      emitters.remove(emitter);
      return emitters.isEmpty() ? null : emitters;
    });
  }

  public Map<UUID, List<SseEmitter>> findAll() {
    return this.data;
  }
}