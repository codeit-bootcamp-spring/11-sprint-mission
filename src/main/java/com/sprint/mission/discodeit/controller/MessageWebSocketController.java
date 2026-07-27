package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

  private final MessageService messageService;

  // 프론트에서 첨부파일이 없는 메시지를 STOMP("/pub/messages")로 처리
  @MessageMapping("/messages")
  public void send(@Payload MessageCreateRequest request) {
    messageService.create(request, null);
  }

}
