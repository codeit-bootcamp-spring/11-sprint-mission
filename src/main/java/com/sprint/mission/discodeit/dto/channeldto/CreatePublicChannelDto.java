package com.sprint.mission.discodeit.dto.channeldto;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public record CreatePublicChannelDto(

        String channelName,
        UUID ownerId,
        String channelDescription,
        List<UUID> membersId
) {
}
