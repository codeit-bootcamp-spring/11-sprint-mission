package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ChannelCreateRequest(
    @NotNull ChannelType channelType,
    String channelName,
    String channelDescription,
    List<UUID> userIds
) {

}

