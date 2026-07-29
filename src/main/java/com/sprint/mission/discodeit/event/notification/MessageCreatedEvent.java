package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import java.util.UUID;

public record MessageCreatedEvent(
    UUID channelId,
    UUID authorId,
    String content,
    String channelName,
    MessageDto messageDto
) {

}
