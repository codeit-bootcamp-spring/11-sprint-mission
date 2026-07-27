package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.notification.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.notification.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.notification.S3UploadFailedEvent;
import com.sprint.mission.discodeit.event.user.UserCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserDeletedEvent;
import com.sprint.mission.discodeit.event.user.UserLogInOutEvent;
import com.sprint.mission.discodeit.event.user.UserUpdatedEvent;
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
    String topic = "discodeit.MessageCreatedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    String topic = "discodeit.RoleUpdatedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    String topic = "discodeit.S3UploadFailedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(NotificationCreatedEvent event) {
    String topic = "discodeit.NotificationCreatedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(BinaryContentUpdatedEvent event) {
    String topic = "discodeit.BinaryContentUpdatedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelCreatedEvent event) {
    String topic = "discodeit.ChannelCreatedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelUpdatedEvent event) {
    String topic = "discodeit.ChannelUpdatedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelDeletedEvent event) {
    String topic = "discodeit.ChannelDeletedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(UserCreatedEvent event) {
    String topic = "discodeit.UserCreatedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(UserUpdatedEvent event) {
    String topic = "discodeit.UserUpdatedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(UserDeletedEvent event) {
    String topic = "discodeit.UserDeletedEvent";
    send(topic, event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(UserLogInOutEvent event) {
    String topic = "discodeit.UserLogInOutEvent";
    send(topic, event);
  }

  private void send(String topic, Object event) {
    try {
      // Java → Json 객체로 직렬화
      String payload = objectMapper.writeValueAsString(event);

      // CompletableFuture<K, V> send(String, V)
      // Json(payload) → Kafka value
      kafkaTemplate.send(topic, payload);

      log.info("Kafka 이벤트 발행 완료 topic={}, payload={}", topic, payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패", e);
    }
  }

}
