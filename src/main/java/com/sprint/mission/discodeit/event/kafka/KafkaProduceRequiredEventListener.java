package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.notification.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.binarycontent.S3UploadFailedEvent;
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
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    send(MessageCreatedEvent.TOPIC, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    send(RoleUpdatedEvent.TOPIC, event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    send(S3UploadFailedEvent.TOPIC, event);
  }

  private void send(String topic, Object event) {
    log.debug("kafka publish trial: topic={}, event={}", topic, event);
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(topic, payload)
          .whenComplete((result, ex) -> {
            if (ex != null) {
              log.error("kafka publish fail (broker send): topic={}, event={}", topic, event, ex);
            } else {
              log.info("kafka publish success: topic={}, partition={}, offset={}", topic,
                  result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
          });
    } catch (JsonProcessingException e) {
      log.error("kafka publish fail (serialize): topic={}, event={}", topic, event, e);
    }
  }
}