package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEmitterSender {

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;

  public void deliver(SseMessage message) {
    sseMessageRepository.save(message);

    if (message.receiverIds().isEmpty()) {
      sseEmitterRepository.findAll().forEach((receiverId, emitters) ->
          emitters.forEach(emitter -> sendTo(receiverId, emitter, message)));
      return;
    }

    message.receiverIds().forEach(receiverId ->
        sseEmitterRepository.findAllByReceiverId(receiverId)
            .forEach(emitter -> sendTo(receiverId, emitter, message)));
  }

  public void sendTo(UUID receiverId, SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event()
          .id(message.eventId().toString())
          .name(message.eventName())
          .data(message.data()));
    } catch (IOException | IllegalStateException e) {
      log.warn("SSE 전송 실패, 연결 제거: receiverId={}, eventName={}",
          receiverId, message.eventName(), e);
      sseEmitterRepository.remove(receiverId, emitter);
    }
  }

  public boolean ping(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().comment("ping"));
      return true;
    } catch (IOException | IllegalStateException e) {
      log.debug("SSE ping 실패", e);
      return false;
    }
  }
}