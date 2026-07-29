package com.sprint.mission.discodeit.event.dto;

import java.util.List;
import java.util.UUID;

public record PrivateChannelCreatedEvent(
    List<UUID> participantIds
) {

}
