package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import com.sprint.mission.discodeit.event.realtime.RealtimeEventPublisher;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
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

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;
  private final RealtimeEventPublisher realtimeEventPublisher;

  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(TIMEOUT.toMillis());

    emitter.onCompletion(() -> sseEmitterRepository.remove(receiverId, emitter));
    emitter.onTimeout(() -> sseEmitterRepository.remove(receiverId, emitter));
    emitter.onError(e -> sseEmitterRepository.remove(receiverId, emitter));

    sseEmitterRepository.save(receiverId, emitter);

    try {
      emitter.send(SseEmitter.event()
          .name("connect")
          .data("connected"));
    } catch (IOException e) {
      log.warn("SSE 최초 연결 이벤트 전송 실패 - receiverId: {}", receiverId, e);
      sseEmitterRepository.remove(receiverId, emitter);
      return emitter;
    }

    if (lastEventId != null) {
      List<SseMessage> missed = sseMessageRepository.findAllAfter(lastEventId);
      missed.forEach(message -> sendToEmitter(emitter, message));
    }

    return emitter;
  }

  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    SseMessage message = SseMessage.of(eventName, data);
    realtimeEventPublisher.publishSse(new ArrayList<>(receiverIds), message);
  }

  public void broadcast(String eventName, Object data) {
    SseMessage message = SseMessage.of(eventName, data);
    realtimeEventPublisher.publishSse(null, message);
  }

  private void sendToEmitter(SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event()
          .id(message.id().toString())
          .name(message.eventName())
          .data(message.data()));
    } catch (IOException e) {
      log.debug("SSE 전송 실패, emitter 제거 - eventName: {}", message.eventName(), e);
      emitter.completeWithError(e);
    }
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    sseEmitterRepository.findAll().forEach((receiverId, emitters) ->
        emitters.forEach(emitter -> {
          if (!ping(emitter)) {
            sseEmitterRepository.remove(receiverId, emitter);
          }
        })
    );
  }

  private boolean ping(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event()
          .name("ping")
          .comment("keep-alive"));
      return true;
    } catch (IOException e) {
      log.debug("SSE ping 실패, 연결 만료로 판단", e);
      return false;
    }
  }
}