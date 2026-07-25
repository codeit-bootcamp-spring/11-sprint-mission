package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MessageWebSocketController {

  private final MessageService messageService;

  @MessageMapping("/messages")
  public void createMessage(@Valid @Payload MessageCreateRequest messageCreateRequest) {
    log.info("message websocket create request: request={}", messageCreateRequest);
    this.messageService.createMessage(messageCreateRequest, List.of());
  }
}