package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ChannelCreateRequest(
    @NotNull(message = "channelType은 필수입니다.")
    ChannelType channelType,

    String name,
    String description,
    List<UUID> participantIds
) {

}
