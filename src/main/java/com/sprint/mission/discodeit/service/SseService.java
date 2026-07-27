package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

  private static final Duration TIMEOUT = Duration.ofMinutes(30);
  private static final String CONNECT_EVENT_NAME = "connect";
  private static final String PING_EVENT_NAME = "ping";

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;

  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(TIMEOUT.toMillis());

    emitter.onCompletion(() -> sseEmitterRepository.remove(receiverId, emitter));
    emitter.onTimeout(() -> sseEmitterRepository.remove(receiverId, emitter));
    emitter.onError(e -> sseEmitterRepository.remove(receiverId, emitter));

    sseEmitterRepository.save(receiverId, emitter);

    try {
      emitter.send(SseEmitter.event()
          .id(UUID.randomUUID().toString())
          .name(CONNECT_EVENT_NAME)
          .data("connected"));
    } catch (IOException e) {
      log.warn("SSE 최초 연결 이벤트 전송 실패 - receiverId: {}", receiverId, e);
      sseEmitterRepository.remove(receiverId, emitter);
      return emitter;
    }

    if (lastEventId != null) {
      sseMessageRepository.findAllAfter(lastEventId).stream()
          .filter(message -> message.isTargetOf(receiverId))
          .forEach(message -> sendToEmitter(receiverId, emitter, message));
    }

    return emitter;
  }

  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    SseMessage message = new SseMessage(
        null, eventName, data, new HashSet<>(receiverIds), Instant.now());
    UUID eventId = sseMessageRepository.save(message);
    SseMessage saved = new SseMessage(
        eventId, eventName, data, new HashSet<>(receiverIds), Instant.now());

    for (UUID receiverId : receiverIds) {
      for (SseEmitter emitter : sseEmitterRepository.findAllByReceiverId(receiverId)) {
        sendToEmitter(receiverId, emitter, saved);
      }
    }
  }

  public void broadcast(String eventName, Object data) {
    SseMessage message = new SseMessage(null, eventName, data, null, Instant.now());
    UUID eventId = sseMessageRepository.save(message);
    SseMessage saved = new SseMessage(eventId, eventName, data, null, Instant.now());

    sseEmitterRepository.findAll().forEach((receiverId, emitters) ->
        emitters.forEach(emitter -> sendToEmitter(receiverId, emitter, saved)));
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    sseEmitterRepository.findAll().forEach((receiverId, emitters) ->
        List.copyOf(emitters).forEach(emitter -> {
          if (!ping(emitter)) {
            sseEmitterRepository.remove(receiverId, emitter);
          }
        }));
  }

  private boolean ping(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event()
          .id(UUID.randomUUID().toString())
          .name(PING_EVENT_NAME)
          .data("ping"));
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private void sendToEmitter(UUID receiverId, SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event()
          .id(message.id().toString())
          .name(message.eventName())
          .data(message.data()));
    } catch (IOException e) {
      log.debug("SSE 전송 실패, 연결 종료 처리 - receiverId: {}, eventId: {}", receiverId, message.id());
      sseEmitterRepository.remove(receiverId, emitter);
      emitter.completeWithError(e);
    }
  }
}
