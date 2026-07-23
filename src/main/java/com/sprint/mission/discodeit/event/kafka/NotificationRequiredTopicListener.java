package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("kafka")
public class NotificationRequiredTopicListener {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = KafkaTopics.MESSAGE_CREATED, groupId = "${spring.kafka.consumer.group-id}")
  public void onMessageCreatedEvent(String payload) {
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
  public void onRoleUpdatedEvent(String payload) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(payload, RoleUpdatedEvent.class);
      String content = event.oldRole().name() + " -> " + event.newRole().name();
      notificationService.create(event.userId(), "권한이 변경되었습니다.", content);
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패: topic={}", KafkaTopics.ROLE_UPDATED, e);
    }
  }

  @KafkaListener(topics = KafkaTopics.S3_UPLOAD_FAILED, groupId = "${spring.kafka.consumer.group-id}")
  public void onS3UploadFailedEvent(String payload) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(payload, S3UploadFailedEvent.class);
      String title = "S3 업로드 실패";
      String content = "requestId: " + event.requestId() + ", 원인: " + event.errorMessage();
      userRepository.findAllByRole(Role.ADMIN)
          .forEach(admin -> notificationService.create(admin.getId(), title, content));
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패: topic={}", KafkaTopics.S3_UPLOAD_FAILED, e);
    }
  }
}
