package com.sprint.mission.discodeit.dto.messagedto;

import java.time.Instant;
import java.util.UUID;

public record LastMessageTimeDto(
    UUID channelId,
    Instant lastMessageTime

) {

}
