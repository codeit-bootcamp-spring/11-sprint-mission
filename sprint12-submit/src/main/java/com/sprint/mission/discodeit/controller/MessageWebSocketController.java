package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class MessageWebSocketController {

  private final MessageService messageService;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/messages")
  public void createMessage(@Valid @Payload MessageCreateRequest request) {
    MessageDto createdMessage = messageService.create(request, List.of());

    messagingTemplate.convertAndSend(
        "/sub/channels." + createdMessage.channelId() + ".messages",
        createdMessage
    );
  }
}
