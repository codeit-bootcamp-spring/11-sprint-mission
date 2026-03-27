package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequestMapping("/api/message")
@ResponseBody
@RequiredArgsConstructor
@Controller
public class MessageController {
    private final MessageService messageService;
}
