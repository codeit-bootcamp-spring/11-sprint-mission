package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.websocket.MessagePublishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// 경쟁 컨슈머(discodeit-group)와 달리 인스턴스마다 다른 group id를 써서 전부 다 받게 한다.
@Slf4j
@Profile("kafka")
@RequiredArgsConstructor
@Component
public class BroadcastRequiredTopicListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = MessagePublishedEvent.TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onMessagePublishedEvent(String kafkaEvent) {
    try {
      MessagePublishedEvent event = objectMapper.readValue(kafkaEvent, MessagePublishedEvent.class);
      log.debug("websocket message-published trial: channelId={}", event.channelId());

      this.messagingTemplate.convertAndSend(
          "/sub/channels." + event.channelId() + ".messages", event.message());

      log.info("websocket message-published success: channelId={}", event.channelId());
    } catch (JsonProcessingException e) {
      log.error("websocket message-published fail (deserialize): kafkaEvent={}", kafkaEvent, e);
    }
  }
}