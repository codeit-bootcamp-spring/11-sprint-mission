package com.sprint.mission.discodeit.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseService {

    private final SseEmitterRepository sseEmitterRepository;
    private final SseMessageRespository sseMessageRespository;

    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onCompletion(() -> sseEmitterRepository.remove(receiverId, emitter));
        emitter.onTimeout(() -> sseEmitterRepository.remove(receiverId, emitter));
        emitter.onError(e -> sseEmitterRepository.remove(receiverId, emitter));
        sseEmitterRepository.save(receiverId, emitter);

        ping(emitter);

        sseMessageRespository.findAllAfter(lastEventId, receiverId)
                .forEach(message -> sendToEmitter(emitter, message));

        log.debug("SSE 연결 생성: receiverId={}, lastEventId={}", receiverId, lastEventId);
        return emitter;
    }

    public void send(Collection<UUID> receiverIds, String eventName, Object data) {
        SseMessage message = new SseMessage(
                UUID.randomUUID(), Set.copyOf(receiverIds), eventName, data, Instant.now());
        sseMessageRespository.save(message);
        receiverIds.forEach(receiverId ->
                sseEmitterRepository.findAllByReceiverId(receiverId)
                        .forEach(emitter -> sendToEmitter(emitter, message)));
    }

    public void broadcast(String eventName, Object data) {
        SseMessage message = new SseMessage(
                UUID.randomUUID(), null, eventName, data, Instant.now()
        );
        sseMessageRespository.save(message);
        sseEmitterRepository.findAll().values()
                .forEach(emitters -> emitters.forEach(emitter -> sendToEmitter(emitter, message)));
    }

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanup() {
        log.debug("SSE emitter 정리 시작");
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
            emitter.send(SseEmitter.event().comment("ping"));
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
            log.warn("SSE 이벤트 전송 실패: eventName={}", message.eventName(), e);
        }
    }
}
