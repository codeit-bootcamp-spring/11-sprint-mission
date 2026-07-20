package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = KafkaTopic.MESSAGE_CREATED)
  public void onMessageCreatedEvent(String kafkaEvent) {
    notificationService.createAll(read(kafkaEvent, MessageCreatedEvent.class));
  }

  @KafkaListener(topics = KafkaTopic.ROLE_UPDATED)
  public void onRoleUpdatedEvent(String kafkaEvent) {
    notificationService.create(read(kafkaEvent, RoleUpdatedEvent.class));
  }

  @KafkaListener(topics = KafkaTopic.S3_UPLOAD_FAILED)
  public void onS3UploadFailedEvent(String kafkaEvent) {
    notificationService.createAll(read(kafkaEvent, S3UploadFailedEvent.class));
  }

  private <T> T read(String kafkaEvent, Class<T> eventType) {
    try {
      return objectMapper.readValue(kafkaEvent, eventType);
    } catch (JsonProcessingException e) {
      log.error("Kafka 이벤트 역직렬화 실패: type={}", eventType.getSimpleName(), e);
      throw new RuntimeException(e);
    }
  }
}