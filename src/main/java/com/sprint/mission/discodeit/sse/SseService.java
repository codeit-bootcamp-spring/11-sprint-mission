package com.sprint.mission.discodeit.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final long SSE_TIMEOUT = 1000L * 60 * 30;

    private final SseEmitterRepository sseEmitterRepository;
    private final SseMessageRepository sseMessageRepository;

    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
        sseEmitterRepository.save(receiverId, sseEmitter);
        sseEmitter.onCompletion(() ->
                sseEmitterRepository.delete(receiverId, sseEmitter));
        sseEmitter.onTimeout(() ->
                sseEmitterRepository.delete(receiverId, sseEmitter));
        sseEmitter.onError(error ->
                sseEmitterRepository.delete(receiverId, sseEmitter));

        if (!ping(sseEmitter)) {
            sseEmitterRepository.delete(receiverId, sseEmitter);
            return sseEmitter;
        }

        sseMessageRepository.findAllAfter(lastEventId, receiverId)
                .forEach(message -> send(sseEmitter, message));
        return sseEmitter;
    }

    public void send(
            Collection<UUID> receiverIds,
            String eventName,
            Object data
    ) {
        SseMessage message = new SseMessage(
                UUID.randomUUID(),
                Set.copyOf(receiverIds),
                eventName,
                data
        );
        sseMessageRepository.save(message);

        receiverIds.forEach(receiverId ->
                sseEmitterRepository.findAllByReceiverId(receiverId)
                        .forEach(sseEmitter -> send(sseEmitter, message)));
    }

    public void broadcast(String eventName, Object data) {
        SseMessage message = new SseMessage(
                UUID.randomUUID(),
                Set.of(),
                eventName,
                data
        );
        sseMessageRepository.save(message);
        sseEmitterRepository.findAll()
                .forEach(sseEmitter -> send(sseEmitter, message));
    }

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanUp() {
        sseEmitterRepository.findAll().forEach(this::ping);
    }

    private boolean ping(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event().name("ping").data("ping"));
            return true;
        } catch (IOException e) {
            sseEmitter.complete();
            return false;
        }
    }

    private void send(SseEmitter sseEmitter, SseMessage message) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .id(message.id().toString())
                    .name(message.name())
                    .data(message.data()));
        } catch (IOException e) {
            log.debug("SSE 이벤트 전송 실패: eventId={}", message.id());
            sseEmitter.complete();
        }
    }
}
