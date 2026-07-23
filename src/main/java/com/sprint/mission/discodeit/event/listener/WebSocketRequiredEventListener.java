package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMessage(MessageDto message) {
    messagingTemplate.convertAndSend(
        "/sub/channels." + message.channelId() + ".messages", message
    );
  }
}
