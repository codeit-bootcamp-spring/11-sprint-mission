package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMessage(MessageCreatedEvent event) {
    log.debug("웹소켓 메시지 브로드캐스팅 준비 - channelId: {}, messageId: {}", event.getChannelId(),
        event.getMessageId());

    String destination = "/sub/channels." + event.getChannelId() + ".messages";
    messagingTemplate.convertAndSend(destination, event);

    log.info("웹소켓 브로드캐스팅 완료 - 목적지: {}", destination);
  }
}
