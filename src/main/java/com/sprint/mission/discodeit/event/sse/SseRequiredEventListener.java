package com.sprint.mission.discodeit.event.sse;

import com.sprint.mission.discodeit.service.SseService;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Profile("!kafka")
@RequiredArgsConstructor
@Component
public class SseRequiredEventListener {

  private final SseService sseService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(NotificationCreatedEvent event) {
    log.debug("sse notification-created trial: receiverId={}",
        event.notification().receiverId());
    this.sseService.send(Set.of(event.notification().receiverId()), "notifications.created",
        event.notification());
    log.info("sse notification-created success: receiverId={}",
        event.notification().receiverId());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentStatusUpdatedEvent event) {
    log.debug("sse binary-content-status-updated trial: id={}", event.binaryContent().id());
    dispatch(event.receiverIds(), "binaryContents.updated", event.binaryContent());
    log.info("sse binary-content-status-updated success: id={}", event.binaryContent().id());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelCreatedEvent event) {
    log.debug("sse channel-created trial: id={}", event.channel().id());
    dispatch(event.receiverIds(), "channels.created", event.channel());
    log.info("sse channel-created success: id={}", event.channel().id());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelUpdatedEvent event) {
    log.debug("sse channel-updated trial: id={}", event.channel().id());
    dispatch(event.receiverIds(), "channels.updated", event.channel());
    log.info("sse channel-updated success: id={}", event.channel().id());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelDeletedEvent event) {
    log.debug("sse channel-deleted trial: id={}", event.channel().id());
    dispatch(event.receiverIds(), "channels.deleted", event.channel());
    log.info("sse channel-deleted success: id={}", event.channel().id());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserCreatedEvent event) {
    log.debug("sse user-created trial: id={}", event.user().id());
    this.sseService.broadcast("users.created", event.user());
    log.info("sse user-created success: id={}", event.user().id());
  }

  // 로그인/로그아웃 핸들러는 트랜잭션 밖에서 이 이벤트를 발행하므로, fallbackExecution 없이는
  // AFTER_COMMIT 리스너가 아예 호출되지 않는다.
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(UserUpdatedEvent event) {
    log.debug("sse user-updated trial: id={}", event.user().id());
    this.sseService.broadcast("users.updated", event.user());
    log.info("sse user-updated success: id={}", event.user().id());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserDeletedEvent event) {
    log.debug("sse user-deleted trial: id={}", event.user().id());
    this.sseService.broadcast("users.deleted", event.user());
    log.info("sse user-deleted success: id={}", event.user().id());
  }

  private void dispatch(Set<UUID> receiverIds, String eventName, Object data) {
    if (receiverIds == null) {
      this.sseService.broadcast(eventName, data);
    } else {
      this.sseService.send(receiverIds, eventName, data);
    }
  }
}