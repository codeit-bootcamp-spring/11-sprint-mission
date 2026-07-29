package com.sprint.mission.discodeit.sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseEmitterRepository {

    private final ConcurrentMap<UUID, List<SseEmitter>> data =
            new ConcurrentHashMap<>();

    public void save(UUID receiverId, SseEmitter sseEmitter) {
        data.computeIfAbsent(
                receiverId,
                key -> new CopyOnWriteArrayList<>()
        ).add(sseEmitter);
    }

    public List<SseEmitter> findAllByReceiverId(UUID receiverId) {
        return data.getOrDefault(receiverId, List.of());
    }

    public Collection<SseEmitter> findAll() {
        return data.values().stream()
                .flatMap(Collection::stream)
                .toList();
    }

    public void delete(UUID receiverId, SseEmitter sseEmitter) {
        data.computeIfPresent(receiverId, (key, emitters) -> {
            emitters.remove(sseEmitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
