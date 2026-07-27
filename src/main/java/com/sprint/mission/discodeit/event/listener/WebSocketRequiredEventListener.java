package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

  private final SimpMessagingTemplate messagingTemplate;

  // MessageService.create 메서드 후 클라이언트로 메시지 전송(클라이언트 기준으로는 메시지 수신)
  // WebSocket 전송은 순서가 중요하기 때문에 동기 처리(@Async를 사용하지 않음)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMessage(MessageCreatedEvent event) {

    MessageDto message = event.getData();

    messagingTemplate.convertAndSend("/sub/channels." + event.getData().channelId() + ".messages",
        message);
  }
}
