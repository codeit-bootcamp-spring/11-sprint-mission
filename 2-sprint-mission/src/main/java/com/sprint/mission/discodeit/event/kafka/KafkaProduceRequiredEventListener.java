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
@Profile("kafka")
@Component
@RequiredArgsConstructor
public class KafkaProduceRequiredEventListener {

  public static final String TOPIC_MESSAGE_CREATED = "discodeit.MessageCreatedEvent";
  public static final String TOPIC_ROLE_UPDATED = "discodeit.RoleUpdatedEvent";
  public static final String TOPIC_S3_UPLOAD_FAILED = "discodeit.S3UploadFailedEvent";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    send(TOPIC_MESSAGE_CREATED, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    send(TOPIC_ROLE_UPDATED, event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    send(TOPIC_S3_UPLOAD_FAILED, event);
  }

  private void send(String topic, Object event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(topic, payload)
          .whenComplete((result, ex) -> {
            if (ex != null) {
              log.error("Kafka 발행 실패: topic={}", topic, ex);
            }
          });
      log.info("Kafka 이벤트 발행: topic={}", topic);
    } catch (JsonProcessingException e) {
      log.error("Kafka 이벤트 직렬화 실패: topic={}", topic, e);
    }
  }
}