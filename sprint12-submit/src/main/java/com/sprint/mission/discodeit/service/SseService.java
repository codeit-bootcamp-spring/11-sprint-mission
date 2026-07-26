package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Service
public class SseService {

  private static final long TIMEOUT = 1000L * 60 * 60;
  private static final int MAX_MESSAGE_SIZE = 1000;

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;

  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(TIMEOUT);

    sseEmitterRepository.save(receiverId, emitter);

    emitter.onCompletion(() -> sseEmitterRepository.delete(receiverId, emitter));
    emitter.onTimeout(() -> sseEmitterRepository.delete(receiverId, emitter));
    emitter.onError(error -> sseEmitterRepository.delete(receiverId, emitter));

    ping(emitter);

    if (lastEventId != null) {
      List<SseMessage> missedMessages = sseMessageRepository.findAllByEventIdAfter(lastEventId);

      missedMessages.stream()
          .filter(message -> message.receiverIds().contains(receiverId))
          .forEach(message -> sendToEmitter(emitter, message));
    }

    return emitter;
  }

  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    SseMessage message = sseMessageRepository.save(receiverIds, eventName, data);

    for (UUID receiverId : receiverIds) {
      List<SseEmitter> emitters = sseEmitterRepository.findAllByReceiverId(receiverId);

      for (SseEmitter emitter : emitters) {
        sendToEmitter(emitter, message);
      }
    }

    sseMessageRepository.deleteOldMessages(MAX_MESSAGE_SIZE);
  }

  public void broadcast(String eventName, Object data) {
    Collection<UUID> receiverIds = sseEmitterRepository.findAll().keySet();

    send(receiverIds, eventName, data);
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    sseEmitterRepository.findAll().forEach((receiverId, emitters) -> {
      for (SseEmitter emitter : emitters) {
        boolean alive = ping(emitter);

        if (!alive) {
          sseEmitterRepository.delete(receiverId, emitter);
        }
      }
    });
  }

  private boolean ping(SseEmitter sseEmitter) {
    try {
      sseEmitter.send(SseEmitter.event()
          .name("ping")
          .data("ping"));

      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private void sendToEmitter(SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event()
          .id(message.id().toString())
          .name(message.eventName())
          .data(message.data()));
    } catch (IOException e) {
      emitter.completeWithError(e);
    }
  }
}