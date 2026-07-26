package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Sse", description = "Server-Sent Events API")
public interface SseApi {

  @Operation(summary = "Connect to the SSE stream")
  SseEmitter connect(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails,
      @Parameter(description = "Last received event ID, used to replay missed events on reconnect") UUID lastEventId
  );
}