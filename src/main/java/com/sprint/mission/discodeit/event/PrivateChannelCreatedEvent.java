package com.sprint.mission.discodeit.event;

import java.util.List;
import java.util.UUID;

public record PrivateChannelCreatedEvent(
    UUID channelId,
    List<UUID> participantIds
) {

}