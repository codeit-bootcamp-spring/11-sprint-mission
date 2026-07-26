package com.sprint.mission.discodeit.event.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.sse.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeBroadcastKafkaListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final SseEmitterRepository sseEmitterRepository;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "realtime.websocket-broadcast",
      groupId = "realtime-ws-${INSTANCE_ID:local-${random.uuid}}"
  )
  public void onWebSocketBroadcast(String json) {
    try {
      WebSocketBroadcastEvent event =
          objectMapper.readValue(json, WebSocketBroadcastEvent.class);
      messagingTemplate.convertAndSend(event.destination(), event.payload());
    } catch (Exception e) {
      log.error("WebSocket 브로드캐스트 이벤트 처리 실패", e);
    }
  }

  @KafkaListener(
      topics = "realtime.sse-broadcast",
      groupId = "realtime-sse-${INSTANCE_ID:local-${random.uuid}}"
  )
  public void onSseBroadcast(String json) {
    try {
      SseBroadcastEvent event = objectMapper.readValue(json, SseBroadcastEvent.class);
      SseMessage message = event.message();

      List<UUID> receiverIds = event.receiverIds();
      if (receiverIds == null || receiverIds.isEmpty()) {
        sseEmitterRepository.findAll().values().stream()
            .flatMap(List::stream)
            .forEach(emitter -> sendToEmitter(emitter, message));
      } else {
        receiverIds.forEach(receiverId ->
            sseEmitterRepository.findAllByReceiverId(receiverId)
                .forEach(emitter -> sendToEmitter(emitter, message)));
      }
    } catch (Exception e) {
      log.error("SSE 브로드캐스트 이벤트 처리 실패", e);
    }
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
}