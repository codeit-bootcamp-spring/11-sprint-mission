package com.sprint.mission.discodeit.event.dto;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import java.time.Instant;

public record ChannelDeletedEvent(
    ChannelDto data,
    Instant createdAt
) {

}
