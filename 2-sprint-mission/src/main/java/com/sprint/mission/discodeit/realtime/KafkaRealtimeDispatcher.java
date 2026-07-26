package com.sprint.mission.discodeit.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.SseMessage;
import com.sprint.mission.discodeit.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("kafka")
@RequiredArgsConstructor
public class KafkaRealtimeDispatcher implements RealtimeDispatcher {

  public static final String TOPIC_WEBSOCKET = "discodeit.realtime.websocket";
  public static final String TOPIC_SSE = "discodeit.realtime.sse";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void dispatch(WebSocketMessage message) {
    send(TOPIC_WEBSOCKET, message);
  }

  @Override
  public void dispatch(SseMessage message) {
    send(TOPIC_SSE, message);
  }

  private void send(String topic, Object message) {
    try {
      kafkaTemplate.send(topic, objectMapper.writeValueAsString(message));
    } catch (JsonProcessingException e) {
      log.error("실시간 메시지 직렬화 실패: topic={}", topic, e);
    }
  }
}