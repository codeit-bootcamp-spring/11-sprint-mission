package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@RestController
public class ChannelController {
    private final ChannelService channelService;
}
