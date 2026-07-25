package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseService {

  private static final long TIMEOUT = Duration.ofMinutes(30).toMillis();
  private static final String PING_EVENT_NAME = "ping";

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;

  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    log.debug("sse connect trial: receiverId={}, lastEventId={}", receiverId, lastEventId);
    SseEmitter emitter = new SseEmitter(TIMEOUT);

    emitter.onCompletion(() -> this.sseEmitterRepository.remove(receiverId, emitter));
    emitter.onTimeout(() -> this.sseEmitterRepository.remove(receiverId, emitter));
    emitter.onError(e -> this.sseEmitterRepository.remove(receiverId, emitter));

    // 등록 전 스냅샷: 이 시점 이후 저장되는 메시지는 실시간 push로도 이 emitter에 도달하니,
    // 재생 시 이 경계까지만 보내서 실시간 push와의 중복 전달을 막는다.
    UUID connectionTimeLatestId = this.sseMessageRepository.getLatestEventId();

    this.sseEmitterRepository.save(receiverId, emitter);
    if (!ping(emitter)) {
      return emitter;
    }

    List<SseMessage> missed = connectionTimeLatestId == null
        ? List.of()
        : this.sseMessageRepository.findAllAfter(lastEventId, receiverId);

    int replayedCount = 0;
    for (SseMessage message : missed) {
      if (!sendToEmitter(emitter, message)) {
        break;
      }
      replayedCount++;
      if (message.id().equals(connectionTimeLatestId)) {
        break;
      }
    }

    log.info("sse connect success: receiverId={}, replayed={}", receiverId, replayedCount);
    return emitter;
  }

  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    if (receiverIds.isEmpty()) {
      return;
    }

    // Set.copyOf는 인자가 이미 JDK의 불변 Set(예: 여기서 만든 것)이면 재복사 없이 그대로
    // 반환한다. 레코드 컴팩트 생성자가 다시 Set.copyOf를 하지만 이중 복사 비용은 없다.
    SseMessage message = new SseMessage(Set.copyOf(receiverIds), eventName, data);
    this.sseMessageRepository.save(message);

    receiverIds.forEach(receiverId -> this.sseEmitterRepository.findAllByReceiverId(receiverId)
        .forEach(emitter -> sendToEmitter(emitter, message)));

    log.info("sse send success: receiverIds={}, eventName={}", receiverIds, eventName);
  }

  public void broadcast(String eventName, Object data) {
    SseMessage message = new SseMessage(null, eventName, data);
    this.sseMessageRepository.save(message);

    this.sseEmitterRepository.findAll().values()
        .forEach(emitters -> emitters.forEach(emitter -> sendToEmitter(emitter, message)));

    log.info("sse broadcast success: eventName={}", eventName);
  }

  @Scheduled(fixedDelayString = "${discodeit.sse.cleanup-interval}")
  public void cleanUp() {
    log.debug("sse clean-up trial");
    this.sseEmitterRepository.findAll().values()
        .forEach(emitters -> emitters.forEach(this::ping));
    log.info("sse clean-up success");
  }

  private boolean sendToEmitter(SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event()
          .id(message.id().toString())
          .name(message.eventName())
          .data(message.data()));
      return true;
    } catch (IOException | IllegalStateException e) {
      log.warn("sse send fail, closing emitter: messageId={}", message.id(), e);
      emitter.completeWithError(e);
      return false;
    }
  }

  private boolean ping(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().name(PING_EVENT_NAME).data(""));
      return true;
    } catch (IOException | IllegalStateException e) {
      emitter.completeWithError(e);
      return false;
    }
  }
}