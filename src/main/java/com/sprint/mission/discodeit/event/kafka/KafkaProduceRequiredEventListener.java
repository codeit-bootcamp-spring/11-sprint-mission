package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.notification.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.binarycontent.S3UploadFailedEvent;
import com.sprint.mission.discodeit.event.sse.BinaryContentStatusUpdatedEvent;
import com.sprint.mission.discodeit.event.sse.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.sse.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.sse.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.sse.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.sse.UserCreatedEvent;
import com.sprint.mission.discodeit.event.sse.UserDeletedEvent;
import com.sprint.mission.discodeit.event.sse.UserUpdatedEvent;
import com.sprint.mission.discodeit.event.websocket.MessagePublishedEvent;
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

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessagePublishedEvent event) {
    send(MessagePublishedEvent.TOPIC, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(NotificationCreatedEvent event) {
    send(NotificationCreatedEvent.TOPIC, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(BinaryContentStatusUpdatedEvent event) {
    send(BinaryContentStatusUpdatedEvent.TOPIC, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelCreatedEvent event) {
    send(ChannelCreatedEvent.TOPIC, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelUpdatedEvent event) {
    send(ChannelUpdatedEvent.TOPIC, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelDeletedEvent event) {
    send(ChannelDeletedEvent.TOPIC, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(UserCreatedEvent event) {
    send(UserCreatedEvent.TOPIC, event);
  }

  // 로그인/로그아웃 핸들러가 트랜잭션 밖에서 이 이벤트를 발행하므로 fallbackExecution이 필요하다.
  @Async("eventTaskExecutor")
  @TransactionalEventListener(fallbackExecution = true)
  public void on(UserUpdatedEvent event) {
    send(UserUpdatedEvent.TOPIC, event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(UserDeletedEvent event) {
    send(UserDeletedEvent.TOPIC, event);
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