package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = KafkaTopics.MESSAGE_CREATED, groupId = "${spring.kafka.consumer.group-id}")
  public void onMessageCreated(String payload) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(payload, MessageCreatedEvent.class);
      List<ReadStatus> targets = readStatusRepository
          .findAllByChannelIdAndNotificationEnabledTrueAndUserIdNot(
              event.channelId(), event.authorId());

      String title = event.authorName() + " (#" + event.channelName() + ")";
      targets.forEach(rs -> notificationService.create(rs.getUser().getId(), title, event.content()));
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패: topic={}", KafkaTopics.MESSAGE_CREATED, e);
    }
  }

  @KafkaListener(topics = KafkaTopics.ROLE_UPDATED, groupId = "${spring.kafka.consumer.group-id}")
  public void onRoleUpdated(String payload) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(payload, RoleUpdatedEvent.class);
      String content = event.oldRole().name() + " -> " + event.newRole().name();
      notificationService.create(event.userId(), "권한이 변경되었습니다.", content);
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패: topic={}", KafkaTopics.ROLE_UPDATED, e);
    }
  }

  @KafkaListener(topics = KafkaTopics.S3_UPLOAD_FAILED, groupId = "${spring.kafka.consumer.group-id}")
  public void onS3UploadFailed(String payload) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(payload, S3UploadFailedEvent.class);
      log.error("S3 업로드 실패 알림 - requestId: {}, binaryContentId: {}, 원인: {}",
          event.requestId(), event.binaryContentId(), event.errorMessage());
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패: topic={}", KafkaTopics.S3_UPLOAD_FAILED, e);
    }
  }
}
