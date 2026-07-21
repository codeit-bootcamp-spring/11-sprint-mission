package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Controller
@RequiredArgsConstructor
@Validated
public class MessageWebSocketController {

  private final MessageService messageService;

  @MessageMapping("/messages")
  public void handleMassage(@Valid MessageCreateRequest request) {
    log.debug("STOMP 웹소켓 메시지 수신 - channelId: {}", request.channelId());
    messageService.create(request, null);
    log.info("STOMP 웹소켓 메시지 처리 완료 - authorId: {}", request.authorId());
  }
}
