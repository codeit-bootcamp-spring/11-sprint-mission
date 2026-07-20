package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("kafka")
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = MessageCreatedEvent.TOPIC)
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);
      log.debug("notification message-created trial: channelId={}, authorId={}",
          event.channelId(), event.authorId());
      String title = "%s (#%s)".formatted(event.authorName(), event.channelName());

      List<UUID> receiverIds = this.readStatusRepository
          .findAllByChannelIdAndNotificationEnabledTrue(event.channelId())
          .stream()
          .map(ReadStatus::getUser)
          .map(User::getId)
          .filter(userId -> !userId.equals(event.authorId()))
          .toList();
      this.notificationService.createNotification(receiverIds, title, event.content());

      log.info("notification message-created success: channelId={}", event.channelId());
    } catch (JsonProcessingException e) {
      log.error("notification message-created fail (deserialize): kafkaEvent={}", kafkaEvent, e);
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = RoleUpdatedEvent.TOPIC)
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
      log.debug("notification role-updated trial: userId={}, oldRole={}, newRole={}",
          event.userId(), event.oldRole(), event.newRole());
      String content = "%s -> %s".formatted(event.oldRole(), event.newRole());

      this.notificationService.createNotification(
          List.of(event.userId()), "권한이 변경되었습니다.", content);

      log.info("notification role-updated success: userId={}", event.userId());
    } catch (JsonProcessingException e) {
      log.error("notification role-updated fail (deserialize): kafkaEvent={}", kafkaEvent, e);
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = S3UploadFailedEvent.TOPIC)
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
      log.debug("notification s3-upload-failed trial: binaryContentId={}",
          event.binaryContentId());
      String content = "RequestId: %s\nBinaryContentId: %s\nError: %s".formatted(
          event.requestId(), event.binaryContentId(), event.errorMessage());

      List<UUID> adminIds = this.userRepository.findAllByRole(Role.ADMIN).stream()
          .map(User::getId)
          .toList();
      this.notificationService.createNotification(adminIds, "S3 파일 업로드 실패", content);

      log.info("notification s3-upload-failed success: binaryContentId={}",
          event.binaryContentId());
    } catch (JsonProcessingException e) {
      log.error("notification s3-upload-failed fail (deserialize): kafkaEvent={}", kafkaEvent, e);
      throw new RuntimeException(e);
    }
  }
}