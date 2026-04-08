package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.ChannelType;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PrivateChannelRequest(
    List<UUID> participantIds
) {

}
