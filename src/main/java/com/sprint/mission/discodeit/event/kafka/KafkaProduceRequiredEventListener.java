package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.dto.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.dto.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.dto.S3UploadFailedEvent;
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
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    sendToKafka(event);
  }

  private <T> void sendToKafka(T event) {
    String eventName = event.getClass().getSimpleName();
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("discodeit.".concat(event.getClass().getSimpleName()), payload);

      log.info("Kafka 이벤트 발행 완료 - Topic: discodeit.{}", eventName);
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패 - {}", eventName, e);
      throw new RuntimeException(e);
    }
  }
}
