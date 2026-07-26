package com.sprint.mission.discodeit.event.sse;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import java.util.Set;
import java.util.UUID;

public record ChannelDeletedEvent(
    Set<UUID> receiverIds,
    ChannelResponse channel
) {

  public static final String TOPIC = "discodeit.ChannelDeletedEvent";
}