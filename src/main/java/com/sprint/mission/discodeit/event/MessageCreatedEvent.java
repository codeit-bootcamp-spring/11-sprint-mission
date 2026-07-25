package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.MessageDto;

import java.util.List;
import java.util.UUID;

public record MessageCreatedEvent(
        MessageDto message,
        String channelName,
        List<UUID> receiverIds
) {
}
