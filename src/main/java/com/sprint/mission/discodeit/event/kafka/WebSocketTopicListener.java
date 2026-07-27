package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketTopicListener {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  @KafkaListener(
      topics = "discodeit.MessageCreatedEvent",
      groupId = "${HOSTNAME:local}-websocket"
  )
  public void onMessageCreated(String kafkaEvent) {

    try {
      MessageCreatedEvent event =
          objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

      MessageDto message = event.getData();

      messagingTemplate.convertAndSend("/sub/channels." + event.getData().channelId() + ".messages",
          message);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
