package com.sprint.mission.discodeit.dto.channel;

import java.util.List;
import java.util.UUID;

public record PrivateChannelCreatedEvent(
    List<UUID> participantIds
) {

}
