package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/read-statuses")
@RequiredArgsConstructor
@RestController
public class ReadStatusController {
    private final ReadStatusService readStatusService;
}
