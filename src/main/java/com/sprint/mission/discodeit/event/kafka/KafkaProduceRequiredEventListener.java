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
@Component
@RequiredArgsConstructor
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    try {
      // Java → Json 객체로 직렬화
      String payload = objectMapper.writeValueAsString(event);

      String topic = "discodeit.MessageCreatedEvent";

      // CompletableFuture<K, V> send(String, V)
      // Json(payload) → Kafka value
      kafkaTemplate.send(topic, payload);

      log.info("Kafka 이벤트 발행 완료 topic={}, payload={}", topic, payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패", e);
    }
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      String topic = "discodeit.RoleUpdatedEvent";
      kafkaTemplate.send(topic, payload);

      log.info("Kafka 이벤트 발행 완료 topic={}, payload={}", topic, payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패", e);
    }
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      String topic = "discodeit.S3UploadFailedEvent";
      kafkaTemplate.send(topic, payload);

      log.info("Kafka 이벤트 발행 완료 topic={}, payload={}", topic, payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패", e);
    }
  }

}
