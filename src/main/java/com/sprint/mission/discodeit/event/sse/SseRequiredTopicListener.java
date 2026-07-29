package com.sprint.mission.discodeit.event.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.binaryContent.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.notification.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserDeletedEvent;
import com.sprint.mission.discodeit.event.user.UserUpdatedEvent;
import com.sprint.mission.discodeit.service.SseService;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Slf4j
@Component
@RequiredArgsConstructor
public class SseRequiredTopicListener {

  private final SseService sseService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "discodeit.NotificationCreatedEvent", groupId = "#{@instanceGroupId}")
  public void onNotificationCreated(String payload) {
    read(payload, NotificationCreatedEvent.class).ifPresent(event ->
        sseService.send(Set.of(event.data().receiverId()), "notifications.created", event.data())
    );
  }

  @KafkaListener(topics = "discodeit.BinaryContentUpdatedEvent", groupId = "#{@instanceGroupId}")
  public void onBinaryContentUpdated(String payload) {
    read(payload, BinaryContentUpdatedEvent.class).ifPresent(event ->
        sseService.send(Set.of(event.ownerId()), "binaryContents.updated", event.data())
    );
  }

  @KafkaListener(topics = "discodeit.ChannelCreatedEvent", groupId = "#{@instanceGroupId}")
  public void onChannelCreated(String payload) {
    read(payload, ChannelCreatedEvent.class).ifPresent(event -> {
      if (event.participantIds().isEmpty()) {
        sseService.broadcast("channels.created", event.data());
      } else {
        sseService.send(event.participantIds(), "channels.created", event.data());
      }
    });
  }

  @KafkaListener(topics = "discodeit.ChannelUpdatedEvent", groupId = "#{@instanceGroupId}")
  public void onChannelUpdated(String payload) {
    read(payload, ChannelUpdatedEvent.class).ifPresent(event ->
        sseService.broadcast("channels.updated", event.data())
    );
  }

  @KafkaListener(topics = "discodeit.ChannelDeletedEvent", groupId = "#{@instanceGroupId}")
  public void onChannelDeleted(String payload) {
    read(payload, ChannelDeletedEvent.class).ifPresent(event -> {
      if (event.participantIds().isEmpty()) {
        sseService.broadcast("channels.deleted", event.channelId());
      } else {
        sseService.send(event.participantIds(), "channels.deleted", event.channelId());
      }
    });
  }

  @KafkaListener(topics = "discodeit.UserCreatedEvent", groupId = "#{@instanceGroupId}")
  public void onUserCreated(String payload) {
    read(payload, UserCreatedEvent.class).ifPresent(event ->
        sseService.broadcast("users.created", event.data())
    );
  }

  @KafkaListener(topics = "discodeit.UserUpdatedEvent", groupId = "#{@instanceGroupId}")
  public void onUserUpdated(String payload) {
    read(payload, UserUpdatedEvent.class).ifPresent(event ->
        sseService.broadcast("users.updated", event.data())
    );
  }

  @KafkaListener(topics = "discodeit.UserDeletedEvent", groupId = "#{@instanceGroupId}")
  public void onUserDeleted(String payload) {
    read(payload, UserDeletedEvent.class).ifPresent(event ->
        sseService.broadcast("users.deleted", event.userId())
    );
  }

  private <T> Optional<T> read(String payload, Class<T> type) {
    try {
      return Optional.of(objectMapper.readValue(payload, type));
    } catch (JsonProcessingException e) {
      log.error("Kafka SSE 이벤트 역직렬화 실패 - type: {}, payload: {}", type.getSimpleName(), payload);
      return Optional.empty();
    }
  }

}
