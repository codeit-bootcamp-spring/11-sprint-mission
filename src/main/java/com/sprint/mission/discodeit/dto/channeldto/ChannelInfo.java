package com.sprint.mission.discodeit.dto.channeldto;

import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelInfo(

    UUID id,
    ChannelType type,
    String name,
    String description,
    List<UUID> participantIds,
    Instant lastMessageAt

) {

}
