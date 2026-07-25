package com.sprint.mission.discodeit.event.sse;

import com.sprint.mission.discodeit.service.SseService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
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
    if (event.receiverIds() == null) {
      this.sseService.broadcast("binaryContents.updated", event.binaryContent());
    } else {
      this.sseService.send(event.receiverIds(), "binaryContents.updated", event.binaryContent());
    }
    log.info("sse binary-content-status-updated success: id={}", event.binaryContent().id());
  }
}