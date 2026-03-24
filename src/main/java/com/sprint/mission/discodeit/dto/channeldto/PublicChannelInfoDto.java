package com.sprint.mission.discodeit.dto.channeldto;

import com.sprint.mission.discodeit.entity.Channel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicChannelInfoDto(


        UUID channelId,
        UUID ownerId,
        String channelName,
        Channel.ChannelType channelType,
        String channelDescription,
        Instant LastMessageTime
) {
}
