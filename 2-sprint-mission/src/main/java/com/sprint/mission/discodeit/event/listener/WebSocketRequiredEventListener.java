package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.WebSocketMessage;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.realtime.RealtimeDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

  private final RealtimeDispatcher realtimeDispatcher;

  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    MessageDto.Response message = event.message();
    String destination = "/sub/channels." + message.channelId() + ".messages";

    realtimeDispatcher.dispatch(new WebSocketMessage(destination, message));
    log.debug("웹소켓 메시지 전파: destination={}", destination);
  }
}