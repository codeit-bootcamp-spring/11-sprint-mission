package com.sprint.mission.discodeit.event.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.sse.SseMessage;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeEventPublisher {

  private static final String WEBSOCKET_TOPIC = "realtime.websocket-broadcast";
  private static final String SSE_TOPIC = "realtime.sse-broadcast";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final SseMessageRepository sseMessageRepository;   // 추가

  public void publishWebSocket(String destination, Object payload) {
    try {
      String json = objectMapper.writeValueAsString(
          new WebSocketBroadcastEvent(destination, payload));
      kafkaTemplate.send(WEBSOCKET_TOPIC, destination, json);
    } catch (Exception e) {
      log.error("WebSocket 브로드캐스트 이벤트 발행 실패 - destination: {}", destination, e);
    }
  }

  public void publishSse(List<UUID> receiverIds, SseMessage message) {
    sseMessageRepository.save(message);   // 발행 인스턴스에서 1회만 저장
    try {
      String json = objectMapper.writeValueAsString(
          new SseBroadcastEvent(receiverIds, message));
      kafkaTemplate.send(SSE_TOPIC, message.id().toString(), json);
    } catch (Exception e) {
      log.error("SSE 브로드캐스트 이벤트 발행 실패 - eventName: {}", message.eventName(), e);
    }
  }
}