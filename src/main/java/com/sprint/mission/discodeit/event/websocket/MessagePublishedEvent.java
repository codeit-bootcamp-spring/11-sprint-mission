package com.sprint.mission.discodeit.event.websocket;

import com.sprint.mission.discodeit.dto.message.MessageResponse;
import java.util.UUID;

public record MessagePublishedEvent(
    UUID channelId,
    MessageResponse message
) {

}