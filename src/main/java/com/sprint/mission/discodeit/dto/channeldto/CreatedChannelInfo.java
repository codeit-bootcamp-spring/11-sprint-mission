package com.sprint.mission.discodeit.dto.channeldto;

import com.sprint.mission.discodeit.entity.Channel;

import java.time.Instant;
import java.util.UUID;

public record CreatedChannelInfo(

    UUID id,
    Instant createdAt,
    Instant updatedAt,
    Channel.ChannelType type,
    String name,
    String description
) {

}
