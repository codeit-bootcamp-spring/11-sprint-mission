package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import com.sprint.mission.discodeit.service.SseService;
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
public class BasicSseService implements SseService {

  private static final Long TIMEOUT = 1000L * 60 * 30;

  private final SseEmitterRepository emitterRepository;
  private final SseMessageRepository messageRepository;

  @Override
  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(TIMEOUT);
    emitterRepository.save(receiverId, emitter);

    emitter.onCompletion(() -> emitterRepository.delete(receiverId, emitter));
    emitter.onTimeout(() -> emitterRepository.delete(receiverId, emitter));
    emitter.onError((e) -> emitterRepository.delete(receiverId, emitter));

    ping(emitter);

    if (lastEventId != null) {
      List<SseMessage> missingEvents = messageRepository.findEventsAfter(lastEventId);
      for (SseMessage message : missingEvents) {
        sendToClient(emitter, message);
      }
    }

    return emitter;
  }

  @Override
  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    UUID eventId = UUID.randomUUID();
    SseMessage message = new SseMessage(eventId, eventName, data);
    messageRepository.save(eventId, message);

    for (UUID receiverId : receiverIds) {
      List<SseEmitter> emitters = emitterRepository.findAllByUserId(receiverId);
      for (SseEmitter emitter : emitters) {
        sendToClient(emitter, message);
      }
    }
  }

  @Override
  public void broadcast(String eventName, Object data) {
    UUID eventId = UUID.randomUUID();
    SseMessage message = new SseMessage(eventId, eventName, data);
    messageRepository.save(eventId, message);

    emitterRepository.findAll().values().forEach(emitters -> {
      for (SseEmitter emitter : emitters) {
        sendToClient(emitter, message);
      }
    });
  }

  @Override
  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    emitterRepository.findAll().forEach((userId, emitters) -> {
      List<SseEmitter> deadEmitters = new ArrayList<>();
      for (SseEmitter emitter : emitters) {
        if (!ping(emitter)) {
          deadEmitters.add(emitter);
        }
      }
      deadEmitters.forEach(emitter -> emitterRepository.delete(userId, emitter));
    });
    log.info("SSE 만료 연결 주기적 정리 완료");
  }

  private boolean ping(SseEmitter sseEmitter) {
    try {
      sseEmitter.send(SseEmitter.event().name("ping").data("pong"));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private void sendToClient(SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event()
          .id(message.id().toString())
          .name(message.name())
          .data(message.data()));
    } catch (Exception e) {
      emitter.completeWithError(e);
    }
  }
}