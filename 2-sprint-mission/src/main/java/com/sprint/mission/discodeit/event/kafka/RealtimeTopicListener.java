package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.SseMessage;
import com.sprint.mission.discodeit.dto.WebSocketMessage;
import com.sprint.mission.discodeit.realtime.KafkaRealtimeDispatcher;
import com.sprint.mission.discodeit.service.SseEmitterSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("kafka")
@RequiredArgsConstructor
public class RealtimeTopicListener {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;
  private final SseEmitterSender sseEmitterSender;

  @KafkaListener(
      topics = KafkaRealtimeDispatcher.TOPIC_WEBSOCKET,
      groupId = "#{realtimeGroupId}")
  public void onWebSocketMessage(String payload) {
    WebSocketMessage message = read(payload, WebSocketMessage.class);
    messagingTemplate.convertAndSend(message.destination(), message.data());
  }

  @KafkaListener(
      topics = KafkaRealtimeDispatcher.TOPIC_SSE,
      groupId = "#{realtimeGroupId}")
  public void onSseMessage(String payload) {
    sseEmitterSender.deliver(read(payload, SseMessage.class));
  }

  private <T> T read(String payload, Class<T> type) {
    try {
      return objectMapper.readValue(payload, type);
    } catch (JsonProcessingException e) {
      log.error("실시간 메시지 역직렬화 실패: type={}", type.getSimpleName(), e);
      throw new IllegalArgumentException("역직렬화 실패", e);
    }
  }
}