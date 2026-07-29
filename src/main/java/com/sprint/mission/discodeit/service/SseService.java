package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.time.Duration;
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

  private final SseEmitterRepository emitterRepository;
  private final SseMessageRepository sseMessageRepository;

  private static final Long TIMEOUT = Duration.ofMinutes(30).toMillis();

  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    log.debug("SSE 연결 시도 - receiverId: {}", receiverId);
    SseEmitter emitter = new SseEmitter(TIMEOUT);

    emitter.onCompletion(() -> {
      log.debug("SSE 연결 정상 종료 - receiverId: {}", receiverId);
      emitterRepository.remove(receiverId, emitter);
    });
    emitter.onTimeout(() -> {
      log.debug("SSE 연결 타임아웃 - receiverId: {}, timeoutMs: {}", receiverId, TIMEOUT);
      emitterRepository.remove(receiverId, emitter);
    });
    emitter.onError((e) -> {
      log.error("SSE 연결 중 에러 발생 - receiverId: {}", receiverId, e);
      emitterRepository.remove(receiverId, emitter);
    });

    emitterRepository.save(receiverId, emitter);
    log.debug("SSE Emitter 저장 완료 - receiverId: {}", receiverId);

    sendToClient(emitter, receiverId, "ping", "connected!");

    if (lastEventId != null) {
      log.info("유실된 SSE 메시지 복원 시도 - receiverId: {}, lastEventId: {}", receiverId, lastEventId);
      List<SseMessage> missedMessages = sseMessageRepository.findAllAfter(lastEventId);
      missedMessages.forEach(
          message -> sendToClient(emitter, receiverId, message.eventName(), message.data()));
    }

    return emitter;
  }

  public void send(List<UUID> receiverIds, String eventName, Object data) {
    UUID eventId = sseMessageRepository.save(new SseMessage(null, eventName, data));
    log.debug("SSE 개별 전송 - eventName: {}, 대상자 수: {}", eventName, receiverIds.size());

    receiverIds.forEach(receiverId -> {
      List<SseEmitter> emitters = emitterRepository.findAllByReceiverId(receiverId);
      emitters.forEach(emitter -> sendToClient(emitter, receiverId, eventId, eventName, data));
    });
  }

  public void broadcast(String eventName, Object data) {
    UUID eventId = sseMessageRepository.save(new SseMessage(null, eventName, data));
    log.debug("SSE 브로드캐스트 전송 - eventName: {}", eventName);

    emitterRepository.findAll().forEach((receiverId, emitters) -> emitters.forEach(
        emitter -> sendToClient(emitter, receiverId, eventId, eventName, data)));
  }

  private void sendToClient(SseEmitter emitter, UUID receiverId, Object eventId, String eventName,
      Object data) {
    try {
      emitter.send(SseEmitter.event()
          .id(eventId.toString())
          .name(eventName)
          .data(data));
    } catch (IOException e) {
      log.warn("SSE 메시지 전송 실패 (클라이언트 연결 끊김) - receiverId: {}", receiverId);
      emitterRepository.remove(receiverId, emitter);
    }
  }

  // Ping 전용 메서드
  private void sendToClient(SseEmitter emitter, UUID receiverId, String eventName, Object data) {
    sendToClient(emitter, receiverId, "ping-id", eventName, data);
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    log.info("SSE 스케줄러 - 죽은 연결 청소 시작");
    emitterRepository.findAll().forEach((receiverId, emitters) -> emitters.forEach(emitter ->
        ping(emitter, receiverId)
    ));
  }

  private boolean ping(SseEmitter emitter, UUID receiverId) {
    try {
      emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
      return true;
    } catch (IOException e) {
      log.debug("SSE 핑 실패, 연결 삭제 - receiverId: {}", receiverId);
      emitterRepository.remove(receiverId, emitter);
      return false;
    }
  }
}
