package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MessageWebSocketController {

    private final MessageService messageService;

    @MessageMapping("/messages")
    public void create(MessageCreateRequest messageCreateRequest) {
        log.info("웹소켓 메시지 생성 요청: request={}", messageCreateRequest);
        messageService.create(messageCreateRequest, List.of());
    }
}
