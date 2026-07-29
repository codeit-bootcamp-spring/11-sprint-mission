package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.binaryContent.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.notification.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.notification.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.s3.S3UploadFailedEvent;
import com.sprint.mission.discodeit.event.user.UserCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserDeletedEvent;
import com.sprint.mission.discodeit.event.user.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    publish("discodeit.MessageCreatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    publish("discodeit.RoleUpdatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelCreatedEvent event) {
    publish("discodeit.ChannelCreatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelUpdatedEvent event) {
    publish("discodeit.ChannelUpdatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelDeletedEvent event) {
    publish("discodeit.ChannelDeletedEvent", event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(UserCreatedEvent event) {
    publish("discodeit.UserCreatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(UserUpdatedEvent event) {
    publish("discodeit.UserUpdatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(UserDeletedEvent event) {
    publish("discodeit.UserDeletedEvent", event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(NotificationCreatedEvent event) {
    publish("discodeit.NotificationCreatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(BinaryContentUpdatedEvent event) {
    publish("discodeit.BinaryContentUpdatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    publish("discodeit.S3UploadFailedEvent", event);
  }

  private void publish(String topic, Object event) {
    try {
      kafkaTemplate.send(topic, objectMapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패 - topic: {}", topic, e);
    }
  }

}
