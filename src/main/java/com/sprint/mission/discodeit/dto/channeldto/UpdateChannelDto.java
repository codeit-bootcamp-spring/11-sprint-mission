package com.sprint.mission.discodeit.dto.channeldto;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.UUID;

public record UpdateChannelDto(

        UUID channelId,
        String ChannelName,
        UUID ownerId,
        String ChannelDescription
) {
}
