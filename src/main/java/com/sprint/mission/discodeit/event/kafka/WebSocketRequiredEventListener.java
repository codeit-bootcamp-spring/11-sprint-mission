package com.sprint.mission.discodeit.event.kafka;

import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Profile("!kafka")
@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMessage(MessageCreatedEvent event) {
    messagingTemplate.convertAndSend(
        "/sub/channels." + event.channelId() + ".messages", event.messageDto()
    );
  }

}
