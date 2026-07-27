package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.sse.SseEvents.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.sse.SseEvents.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.sse.SseEvents.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.sse.SseEvents.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.sse.SseEvents.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.sse.SseEvents.UserCreatedEvent;
import com.sprint.mission.discodeit.event.sse.SseEvents.UserDeletedEvent;
import com.sprint.mission.discodeit.event.sse.SseEvents.UserUpdatedEvent;
import com.sprint.mission.discodeit.service.SseService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseRequiredEventListener {

  private final SseService sseService;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  // 비즈니스 로직에서 발생한 로컬 이벤트를 Kafka 토픽으로 발행

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishNotificationCreated(NotificationCreatedEvent event) {
    kafkaTemplate.send("sse.notifications.created", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishChannelCreated(ChannelCreatedEvent event) {
    kafkaTemplate.send("sse.channels.created", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishChannelUpdated(ChannelUpdatedEvent event) {
    kafkaTemplate.send("sse.channels.updated", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishChannelDeleted(ChannelDeletedEvent event) {
    kafkaTemplate.send("sse.channels.deleted", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishUserCreated(UserCreatedEvent event) {
    kafkaTemplate.send("sse.users.created", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishUserUpdated(UserUpdatedEvent event) {
    kafkaTemplate.send("sse.users.updated", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishUserDeleted(UserDeletedEvent event) {
    kafkaTemplate.send("sse.users.deleted", event);
  }

  @EventListener
  public void publishBinaryContentUpdated(BinaryContentUpdatedEvent event) {
    kafkaTemplate.send("sse.binaryContents.updated", event);
  }

  // Kafka에서 이벤트를 수신하여 모든 서버 인스턴스가 실제 SSE 발송

  @KafkaListener(topics = "sse.notifications.created", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
  public void consumeNotificationCreated(NotificationCreatedEvent event) {
    sseService.send(Set.of(event.data().receiverId()), "notifications.created", event.data());
  }

  @KafkaListener(topics = "sse.channels.created", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
  public void consumeChannelCreated(ChannelCreatedEvent event) {
    if (event.participantIds() != null && !event.participantIds().isEmpty()) {
      sseService.send(event.participantIds(), "channels.created", event.data());
    } else {
      sseService.broadcast("channels.created", event.data());
    }
  }

  @KafkaListener(topics = "sse.channels.updated", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
  public void consumeChannelUpdated(ChannelUpdatedEvent event) {
    sseService.broadcast("channels.updated", event.data());
  }

  @KafkaListener(topics = "sse.channels.deleted", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
  public void consumeChannelDeleted(ChannelDeletedEvent event) {
    sseService.broadcast("channels.deleted", event.data());
  }

  @KafkaListener(topics = "sse.users.created", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
  public void consumeUserCreated(UserCreatedEvent event) {
    sseService.broadcast("users.created", event.data());
  }

  @KafkaListener(topics = "sse.users.updated", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
  public void consumeUserUpdated(UserUpdatedEvent event) {
    sseService.broadcast("users.updated", event.data());
  }

  @KafkaListener(topics = "sse.users.deleted", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
  public void consumeUserDeleted(UserDeletedEvent event) {
    sseService.broadcast("users.deleted", event.data());
  }

  @KafkaListener(topics = "sse.binaryContents.updated", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
  public void consumeBinaryContentUpdated(BinaryContentUpdatedEvent event) {
    sseService.broadcast("binaryContents.updated", event);
  }
}