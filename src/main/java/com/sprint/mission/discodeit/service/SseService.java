package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

  private final SseEmitterRepository emitterRepository;
  private final SseMessageRepository messageRepository;

  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(TIMEOUT.toMillis());

    emitter.onCompletion(() -> emitterRepository.remove(receiverId, emitter));
    emitter.onTimeout(() -> emitterRepository.remove(receiverId, emitter));
    emitter.onError(e -> emitterRepository.remove(receiverId, emitter));

    emitterRepository.save(receiverId, emitter);

    // 최초 연결 확인용 더미 이벤트
    try {
      emitter.send(SseEmitter.event().name("connect").data("connected"));
    } catch (IOException e) {
      emitterRepository.remove(receiverId, emitter);
      return emitter;
    }

    if (lastEventId != null) {
      if (!messageRepository.isRestorable(lastEventId)) {
        log.warn("SSE 이벤트 복원 실패 - 너무 오래된 lastEventId: {}, receiverId: {}", lastEventId, receiverId);
        sendResyncSignal(emitter, receiverId);
      } else {
        List<SseMessage> missed = messageRepository.findAllAfter(lastEventId);
        missed.stream()
            .filter(m -> m.receiverId() == null || m.receiverId().equals(receiverId))
            .forEach(m -> sendToEmitter(emitter, m.eventId(), m.eventName(), m.data()));
      }
    }

    return emitter;
  }

  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    receiverIds.forEach(receiverId -> {
      UUID eventId = messageRepository.save(receiverId, eventName, data);
      emitterRepository.findAllByReceiverId(receiverId)
          .forEach(emitter -> sendToEmitter(emitter, eventId, eventName, data));
    });
  }

  public void broadcast(String eventName, Object data) {
    UUID eventId = messageRepository.save(null, eventName, data);
    emitterRepository.findAll().forEach((receiverId, emitters) ->
        emitters.forEach(emitter -> sendToEmitter(emitter, eventId, eventName, data)));
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    emitterRepository.findAll().forEach((receiverId, emitters) ->
        emitters.forEach(emitter -> {
          if (!ping(emitter)) {
            emitterRepository.remove(receiverId, emitter);
          }
        }));
  }

  private boolean ping(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().name("ping").data("ping"));
      return true;
    } catch (IOException e) {
      return false; // 죽은 연결
    }
  }

  private void sendToEmitter(SseEmitter emitter, UUID eventId, String eventName, Object data) {
    try {
      emitter.send(SseEmitter.event().id(eventId.toString()).name(eventName).data(data));
    } catch (IOException e) {
      // onError 콜백이 처리
    }
  }

  private void sendResyncSignal(SseEmitter emitter, UUID receiverId) {
    try {
      emitter.send(SseEmitter.event()
          .name("resync")
          .data(Map.of("reason", "event_history_expired")));
    } catch (IOException e) {
      emitterRepository.remove(receiverId, emitter);
    }
  }

}
