package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.notification.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserDeletedEvent;
import com.sprint.mission.discodeit.event.user.UserLogInOutEvent;
import com.sprint.mission.discodeit.event.user.UserUpdatedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.sse.service.SseService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final SseService sseService;
  private final UserService userService;
  private final ReadStatusRepository readStatusRepository;

  @KafkaListener(
      topics = "discodeit.NotificationCreatedEvent",
      groupId = "${HOSTNAME:local}-sse"
  )
  public void onNotificationCreated(String kafkaEvent) {

    try {
      NotificationCreatedEvent event =
          objectMapper.readValue(kafkaEvent, NotificationCreatedEvent.class);

      NotificationDto dto = event.getData();

      sseService.send(List.of(dto.receiverId()), "notifications.created", dto);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(
      topics = "discodeit.BinaryContentUpdatedEvent",
      groupId = "${HOSTNAME:local}-sse"
  )
  public void onBinaryContentUpdated(String kafkaEvent) {

    try {
      BinaryContentUpdatedEvent event =
          objectMapper.readValue(kafkaEvent, BinaryContentUpdatedEvent.class);

      BinaryContentDto dto = event.getTo();

      // receiverId가 존재할 때 → UserService의 create, update 메서드의 프로필 이미지
      if (event.getReceiverId() != null) {
        sseService.send(List.of(event.getReceiverId()), "binaryContents.updated", dto);
      }
      // channelId가 존재할 때 → MessageService의 create 메서드의 첨부파일
      else if (event.getChannelId() != null) {
        List<UUID> receiverIds = readStatusRepository.findByChannelId(event.getChannelId()).stream()
            .map(readStatus -> readStatus.getUser().getId())
            .toList();

        sseService.send(receiverIds, "binaryContents.updated", dto);

      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(
      topics = "discodeit.ChannelCreatedEvent",
      groupId = "${HOSTNAME:local}-sse"
  )
  public void onChannelCreated(String kafkaEvent) {

    try {
      ChannelCreatedEvent event =
          objectMapper.readValue(kafkaEvent, ChannelCreatedEvent.class);

      ChannelDto dto = event.getData();

      // 생성하려는 채널이 PUBLIC 채널이면
      if (event.getReceiverIds() == null) {
        sseService.broadcast("channels.created", dto);
      }
      // 생성하려는 채널이 PRIVATE 채널이면
      else {
        sseService.send(event.getReceiverIds(), "channels.created", dto);
      }

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(
      topics = "discodeit.ChannelUpdatedEvent",
      groupId = "${HOSTNAME:local}-sse"
  )
  public void onChannelUpdated(String kafkaEvent) {

    try {
      ChannelUpdatedEvent event =
          objectMapper.readValue(kafkaEvent, ChannelUpdatedEvent.class);

      ChannelDto dto = event.getTo();

      sseService.broadcast("channels.updated", dto);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(
      topics = "discodeit.ChannelDeletedEvent",
      groupId = "${HOSTNAME:local}-sse"
  )
  public void onChannelDeleted(String kafkaEvent) {

    try {
      ChannelDeletedEvent event =
          objectMapper.readValue(kafkaEvent, ChannelDeletedEvent.class);

      ChannelDto dto = event.getData();

      // PUBLIC 채널
      if (event.getReceiverIds() == null) {
        sseService.broadcast("channels.deleted", dto);
      }
      // PRIVATE 채널
      else {
        sseService.send(event.getReceiverIds(), "channels.deleted", dto);
      }

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(
      topics = "discodeit.UserCreatedEvent",
      groupId = "${HOSTNAME:local}-sse"
  )
  public void onUserCreated(String kafkaEvent) {

    try {
      UserCreatedEvent event =
          objectMapper.readValue(kafkaEvent, UserCreatedEvent.class);

      UserDto dto = event.getData();

      sseService.broadcast("users.created", dto);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(
      topics = "discodeit.UserUpdatedEvent",
      groupId = "${HOSTNAME:local}-sse"
  )
  public void onUserUpdated(String kafkaEvent) {

    try {
      UserUpdatedEvent event =
          objectMapper.readValue(kafkaEvent, UserUpdatedEvent.class);

      UserDto dto = event.getTo();

      sseService.broadcast("users.updated", dto);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(
      topics = "discodeit.UserDeletedEvent",
      groupId = "${HOSTNAME:local}-sse"
  )
  public void onUserDeleted(String kafkaEvent) {

    try {
      UserDeletedEvent event =
          objectMapper.readValue(kafkaEvent, UserDeletedEvent.class);

      UserDto dto = event.getData();

      sseService.broadcast("users.deleted", dto);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(
      topics = "discodeit.UserLogInOutEvent",
      groupId = "${HOSTNAME:local}-sse"
  )
  public void onUserLogInOut(String kafkaEvent) {

    try {
      UserLogInOutEvent event =
          objectMapper.readValue(kafkaEvent, UserLogInOutEvent.class);

      UserDto dto = userService.find(event.getUserId());

      sseService.broadcast("users.updated", dto);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
