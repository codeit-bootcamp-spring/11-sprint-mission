package com.sprint.mission.discodeit.repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterRepository {

  void save(UUID userId, SseEmitter emitter);

  void delete(UUID userId, SseEmitter emitter);

  List<SseEmitter> findAllByUserId(UUID userId);

  ConcurrentMap<UUID, List<SseEmitter>> findAll();
}