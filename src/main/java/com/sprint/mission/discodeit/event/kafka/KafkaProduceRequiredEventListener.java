package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("kafka")
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    try {
      kafkaTemplate.send(KafkaTopics.MESSAGE_CREATED, objectMapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패: topic={}", KafkaTopics.MESSAGE_CREATED, e);
    }
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    try {
      kafkaTemplate.send(KafkaTopics.ROLE_UPDATED, objectMapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패: topic={}", KafkaTopics.ROLE_UPDATED, e);
    }
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    try {
      kafkaTemplate.send(KafkaTopics.S3_UPLOAD_FAILED, objectMapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패: topic={}", KafkaTopics.S3_UPLOAD_FAILED, e);
    }
  }
}
