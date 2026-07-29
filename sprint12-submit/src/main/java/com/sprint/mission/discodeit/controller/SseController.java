package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.SseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
public class SseController {

  private final SseService sseService;

  @GetMapping(path = "/api/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter connect(
      @AuthenticationPrincipal DiscodeitUserDetails principal,
      @RequestHeader(value = "Last-Event-ID", required = false) UUID lastEventIdHeader,
      @RequestParam(value = "lastEventId", required = false) UUID lastEventIdParam
  ) {
    UUID receiverId = principal.getUserDto().id();
    UUID lastEventId = lastEventIdHeader != null ? lastEventIdHeader : lastEventIdParam;

    return sseService.connect(receiverId, lastEventId);
  }
}