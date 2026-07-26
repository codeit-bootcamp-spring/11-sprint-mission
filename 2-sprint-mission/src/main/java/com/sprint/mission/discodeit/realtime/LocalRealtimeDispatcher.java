package com.sprint.mission.discodeit.realtime;

import com.sprint.mission.discodeit.dto.SseMessage;
import com.sprint.mission.discodeit.dto.WebSocketMessage;
import com.sprint.mission.discodeit.service.SseEmitterSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!kafka")
@RequiredArgsConstructor
public class LocalRealtimeDispatcher implements RealtimeDispatcher {

  private final SimpMessagingTemplate messagingTemplate;
  private final SseEmitterSender sseEmitterSender;

  @Override
  public void dispatch(WebSocketMessage message) {
    messagingTemplate.convertAndSend(message.destination(), message.data());
  }

  @Override
  public void dispatch(SseMessage message) {
    sseEmitterSender.deliver(message);
  }
}