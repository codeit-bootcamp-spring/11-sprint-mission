package com.sprint.mission.discodeit.event.channel;

import java.util.List;
import java.util.UUID;

public record ChannelDeletedEvent(
    UUID channelId,
    List<UUID> participantIds
) {

}
