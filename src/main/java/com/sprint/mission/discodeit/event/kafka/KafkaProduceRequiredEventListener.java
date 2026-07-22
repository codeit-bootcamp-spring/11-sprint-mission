package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) {
    publish(KafkaTopics.MESSAGE_CREATED, MessageCreatedMessage.from(event));
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    publish(KafkaTopics.ROLE_UPDATED, RoleUpdatedMessage.from(event));
  }

  private void publish(String topic, Object payload) {
    try {
      String json = objectMapper.writeValueAsString(payload);
      kafkaTemplate.send(topic, json);
      log.debug("Kafka 메시지 발행 완료: topic={}", topic);
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 직렬화 실패: topic={}", topic, e);
    }
  }
}
