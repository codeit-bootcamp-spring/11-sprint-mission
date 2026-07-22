package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent", groupId = "discodeit-group")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);
      log.info("Kafka 메시지 생성 이벤트 수신 - channelId: {}", event.getChannelId());

      User author = userRepository.findById(event.getAuthorId()).orElseThrow();

      List<ReadStatus> activeReadStatuses = readStatusRepository.findByChannelIdAndNotificationEnabledTrue(
          event.getChannelId());

      String title = author.getUsername() + " (#" + event.getChannelName() + ")";
      int count = 0;

      for (ReadStatus readStatus : activeReadStatuses) {
        if (!readStatus.getUser().getId().equals(event.getAuthorId())) {
          notificationService.create(readStatus.getUser().getId(), title, event.getContent());
          count++;
        }
      }

      log.info("Kafka: {}명에게 메시지 알림 전송 완료", count);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.RoleUpdatedEvent", groupId = "discodeit-group")
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
      log.info("Kafka 권한 변경 이벤트 수신 - userId: {}", event.getUserId());

      notificationService.create(
          event.getUserId(),
          "권한이 변경되었습니다.",
          event.getOldRole() + " -> " + event.getNewRole()
      );

      log.info("Kafka: 권한 변경 알림 전송 완료 - userId: {}", event.getUserId());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent", groupId = "discodeit-group")
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
      log.info("Kafka S3 업로드 실패 이벤트 수신");

      notificationService.notifyAdmins("S3 업로드 실패", "S3 업로드 중 문제가 발생했습니다.");
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}