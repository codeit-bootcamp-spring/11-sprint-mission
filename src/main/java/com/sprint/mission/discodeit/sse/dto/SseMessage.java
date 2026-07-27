package com.sprint.mission.discodeit.sse.dto;

import java.util.UUID;

public record SseMessage(
    UUID id,
    String eventName,
    Object data
) {

}
