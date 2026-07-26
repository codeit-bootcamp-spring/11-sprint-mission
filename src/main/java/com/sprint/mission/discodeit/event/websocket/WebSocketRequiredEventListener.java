package com.sprint.mission.discodeit.event.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Profile("!kafka")
@RequiredArgsConstructor
@Component
public class WebSocketRequiredEventListener {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessagePublishedEvent event) {
    log.debug("websocket message-published trial: channelId={}", event.channelId());
    this.messagingTemplate.convertAndSend(
        "/sub/channels." + event.channelId() + ".messages", event.message());
    log.info("websocket message-published success: channelId={}", event.channelId());
  }
}