package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequestMapping("/api/channel")
@ResponseBody
@RequiredArgsConstructor
@Controller
public class ChannelController {
    private final ChannelService channelService;
}
