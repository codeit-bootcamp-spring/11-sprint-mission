package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRequiredTopicListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "discodeit.MessageCreatedEvent",
      groupId = "#{@instanceGroupId}"
  )
  public void onMessageCreatedEvent(String payload) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(payload, MessageCreatedEvent.class);
      messagingTemplate.convertAndSend(
          "/sub/channels." + event.channelId() + ".messages", event.messageDto());
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패", e);
    }
  }
}
