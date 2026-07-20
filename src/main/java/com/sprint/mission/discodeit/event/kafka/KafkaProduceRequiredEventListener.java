package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    send(KafkaTopic.MESSAGE_CREATED, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    send(KafkaTopic.ROLE_UPDATED, event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    send(KafkaTopic.S3_UPLOAD_FAILED, event);
  }

  private void send(String topic, Object event) {
    try {
      kafkaTemplate.send(topic, objectMapper.writeValueAsString(event));
      log.info("Kafka 이벤트 발행: topic={}", topic);
    } catch (JsonProcessingException e) {
      log.error("Kafka 이벤트 직렬화 실패: topic={}", topic, e);
      throw new RuntimeException(e);
    }
  }
}