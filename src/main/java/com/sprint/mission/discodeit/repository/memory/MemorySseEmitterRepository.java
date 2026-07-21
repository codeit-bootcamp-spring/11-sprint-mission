package com.sprint.mission.discodeit.repository.memory;

import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class MemorySseEmitterRepository implements SseEmitterRepository {

  private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

  @Override
  public void save(UUID userId, SseEmitter emitter) {
    data.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
  }

  @Override
  public void delete(UUID userId, SseEmitter emitter) {
    List<SseEmitter> emitters = data.get(userId);
    if (emitters != null) {
      emitters.remove(emitter);
      if (emitters.isEmpty()) {
        data.remove(userId);
      }
    }
  }

  @Override
  public List<SseEmitter> findAllByUserId(UUID userId) {
    return data.getOrDefault(userId, Collections.emptyList());
  }

  @Override
  public ConcurrentMap<UUID, List<SseEmitter>> findAll() {
    return data;
  }
}