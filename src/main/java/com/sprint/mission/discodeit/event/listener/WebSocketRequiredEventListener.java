package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.event.dto.MessageCreatedEvent;
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
    MessageDto messageDto = event.data();

    String destination = String.format("/sub/channels.%s.messages", messageDto.channelId());

    messagingTemplate.convertAndSend(destination, messageDto);

    log.info("웹소켓 메시지 발송 완료 - channelId: {}, destination: {}", messageDto.channelId(), destination);
  }

}
