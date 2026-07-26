package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.sse.BinaryContentStatusUpdatedEvent;
import com.sprint.mission.discodeit.event.sse.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.sse.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.sse.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.sse.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.sse.UserCreatedEvent;
import com.sprint.mission.discodeit.event.sse.UserDeletedEvent;
import com.sprint.mission.discodeit.event.sse.UserUpdatedEvent;
import com.sprint.mission.discodeit.event.websocket.MessagePublishedEvent;
import com.sprint.mission.discodeit.service.SseService;
import java.util.Set;
import java.util.UUID;
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
  private final SseService sseService;
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

  @KafkaListener(
      topics = NotificationCreatedEvent.TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onNotificationCreatedEvent(String kafkaEvent) {
    try {
      NotificationCreatedEvent event = objectMapper.readValue(kafkaEvent,
          NotificationCreatedEvent.class);
      this.sseService.send(Set.of(event.notification().receiverId()), "notifications.created",
          event.notification());
      log.info("sse notification-created success: receiverId={}",
          event.notification().receiverId());
    } catch (JsonProcessingException e) {
      log.error("sse notification-created fail (deserialize): kafkaEvent={}", kafkaEvent, e);
    }
  }

  @KafkaListener(
      topics = BinaryContentStatusUpdatedEvent.TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onBinaryContentStatusUpdatedEvent(String kafkaEvent) {
    try {
      BinaryContentStatusUpdatedEvent event = objectMapper.readValue(kafkaEvent,
          BinaryContentStatusUpdatedEvent.class);
      dispatch(event.receiverIds(), "binaryContents.updated", event.binaryContent());
      log.info("sse binary-content-status-updated success: id={}", event.binaryContent().id());
    } catch (JsonProcessingException e) {
      log.error("sse binary-content-status-updated fail (deserialize): kafkaEvent={}", kafkaEvent,
          e);
    }
  }

  @KafkaListener(
      topics = ChannelCreatedEvent.TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onChannelCreatedEvent(String kafkaEvent) {
    try {
      ChannelCreatedEvent event = objectMapper.readValue(kafkaEvent, ChannelCreatedEvent.class);
      dispatch(event.receiverIds(), "channels.created", event.channel());
      log.info("sse channel-created success: id={}", event.channel().id());
    } catch (JsonProcessingException e) {
      log.error("sse channel-created fail (deserialize): kafkaEvent={}", kafkaEvent, e);
    }
  }

  @KafkaListener(
      topics = ChannelUpdatedEvent.TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onChannelUpdatedEvent(String kafkaEvent) {
    try {
      ChannelUpdatedEvent event = objectMapper.readValue(kafkaEvent, ChannelUpdatedEvent.class);
      dispatch(event.receiverIds(), "channels.updated", event.channel());
      log.info("sse channel-updated success: id={}", event.channel().id());
    } catch (JsonProcessingException e) {
      log.error("sse channel-updated fail (deserialize): kafkaEvent={}", kafkaEvent, e);
    }
  }

  @KafkaListener(
      topics = ChannelDeletedEvent.TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onChannelDeletedEvent(String kafkaEvent) {
    try {
      ChannelDeletedEvent event = objectMapper.readValue(kafkaEvent, ChannelDeletedEvent.class);
      dispatch(event.receiverIds(), "channels.deleted", event.channel());
      log.info("sse channel-deleted success: id={}", event.channel().id());
    } catch (JsonProcessingException e) {
      log.error("sse channel-deleted fail (deserialize): kafkaEvent={}", kafkaEvent, e);
    }
  }

  @KafkaListener(
      topics = UserCreatedEvent.TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onUserCreatedEvent(String kafkaEvent) {
    try {
      UserCreatedEvent event = objectMapper.readValue(kafkaEvent, UserCreatedEvent.class);
      this.sseService.broadcast("users.created", event.user());
      log.info("sse user-created success: id={}", event.user().id());
    } catch (JsonProcessingException e) {
      log.error("sse user-created fail (deserialize): kafkaEvent={}", kafkaEvent, e);
    }
  }

  @KafkaListener(
      topics = UserUpdatedEvent.TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onUserUpdatedEvent(String kafkaEvent) {
    try {
      UserUpdatedEvent event = objectMapper.readValue(kafkaEvent, UserUpdatedEvent.class);
      this.sseService.broadcast("users.updated", event.user());
      log.info("sse user-updated success: id={}", event.user().id());
    } catch (JsonProcessingException e) {
      log.error("sse user-updated fail (deserialize): kafkaEvent={}", kafkaEvent, e);
    }
  }

  @KafkaListener(
      topics = UserDeletedEvent.TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onUserDeletedEvent(String kafkaEvent) {
    try {
      UserDeletedEvent event = objectMapper.readValue(kafkaEvent, UserDeletedEvent.class);
      this.sseService.broadcast("users.deleted", event.user());
      log.info("sse user-deleted success: id={}", event.user().id());
    } catch (JsonProcessingException e) {
      log.error("sse user-deleted fail (deserialize): kafkaEvent={}", kafkaEvent, e);
    }
  }

  private void dispatch(Set<UUID> receiverIds, String eventName, Object data) {
    if (receiverIds == null) {
      this.sseService.broadcast(eventName, data);
    } else {
      this.sseService.send(receiverIds, eventName, data);
    }
  }
}
