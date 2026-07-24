package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("kafka")
@Component
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationService notificationService;

  @KafkaListener(topics = KafkaProduceRequiredEventListener.TOPIC_MESSAGE_CREATED)
  public void onMessageCreatedEvent(String kafkaEvent) {
    notificationService.createForMessage(read(kafkaEvent, MessageCreatedEvent.class));
  }

  @KafkaListener(topics = KafkaProduceRequiredEventListener.TOPIC_ROLE_UPDATED)
  public void onRoleUpdatedEvent(String kafkaEvent) {
    notificationService.createForRoleUpdate(read(kafkaEvent, RoleUpdatedEvent.class));
  }

  @KafkaListener(topics = KafkaProduceRequiredEventListener.TOPIC_S3_UPLOAD_FAILED)
  public void onS3UploadFailedEvent(String kafkaEvent) {
    notificationService.createForS3UploadFailure(read(kafkaEvent, S3UploadFailedEvent.class));
  }

  private <T> T read(String kafkaEvent, Class<T> type) {
    try {
      return objectMapper.readValue(kafkaEvent, type);
    } catch (JsonProcessingException e) {
      log.error("Kafka 이벤트 역직렬화 실패: type={}", type.getSimpleName(), e);
      throw new IllegalArgumentException("Kafka 이벤트 역직렬화 실패", e);
    }
  }
}