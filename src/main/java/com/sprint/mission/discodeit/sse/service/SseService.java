package com.sprint.mission.discodeit.sse.service;

import com.sprint.mission.discodeit.sse.dto.SseMessage;
import com.sprint.mission.discodeit.sse.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.sse.repository.SseMessageRepository;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
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

    // 입력, 반환 둘 다 없음(Runnable)
    emitter.onCompletion(
        () -> emitterRepository.delete(receiverId, emitter)
    );
    emitter.onTimeout(
        () -> emitterRepository.delete(receiverId, emitter)
    );

    // Consumer(void)
    emitter.onError(e ->
        emitterRepository.delete(receiverId, emitter)
    );

    // receiverId는 컨트롤러의 DiscodeitUserDetails 사용자 ID
    emitterRepository.save(receiverId, emitter);

    // 연결 실패 시 즉시 삭제가 아닌 cleanup을 통해 제거
    ping(emitter);

    // SseMessage ID가 있으면 유실된 데이터들을 다시 전송(유실된 데이터 없으면 [].forEach로
    if (lastEventId != null) {
      messageRepository.findAfter(lastEventId) // 유실된 데이터들을 반환
          .forEach(message -> { // 전송
            try {
              emitter.send(
                  SseEmitter.event()
                      .id(message.id().toString())
                      .name(message.eventName())
                      .data(message.data())
              );
            } catch (IOException e) { // 연결이 끊기게 되면
              emitterRepository.delete(receiverId, emitter); // Sse 연결 삭제
            }
          });
    }

    return emitter;
  }

  // receiverIds에 속한 특정 사용자들에게만 메시지를 전송
  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    SseMessage message = new SseMessage(UUID.randomUUID(), eventName, data);

    messageRepository.save(message);

    receiverIds.forEach(receiverId -> emitterRepository.findAllByReceiverId(receiverId)
        .forEach(emitter -> send(receiverId, emitter, message)));
  }

  // 서버가 모든 클라이언트에게 메시지를 보냄
  public void broadcast(String eventName, Object data) {
    SseMessage message = new SseMessage(UUID.randomUUID(), eventName, data);

    messageRepository.save(message);

    emitterRepository.findAll()
        .forEach((receiverId, emitters) -> // BiConsumer(void)
            emitters.forEach(emitter -> send(receiverId, emitter, message))); // Consumer(void)
  }

  // 30분마다 만료(전송 실패)된 SseEmitter 객체 삭제
  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    emitterRepository.findAll()
        .forEach((receiverId, emitters) -> // BiConsumer(void)
            emitters.removeIf(emitter -> !ping(emitter))); // Predicate(boolean)
  }

  // connect, 만료 여부를 확인하기 위해 더미 이벤트를 보냄
  // 전송 성공시 true, 실패 시 false
  private boolean ping(SseEmitter emitter) {

    try {
      emitter.send(SseEmitter.event()
          .name("ping")
          .data(""));

      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private void send(UUID receiverId, SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event()
          .id(message.id().toString())
          .name(message.eventName())
          .data(message.data()));
    } catch (IOException e) {
      emitterRepository.delete(receiverId, emitter);
    }
  }

}
