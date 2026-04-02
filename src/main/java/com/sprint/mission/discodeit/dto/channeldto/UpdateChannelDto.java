package com.sprint.mission.discodeit.dto.channeldto;

import java.util.UUID;

public record UpdateChannelDto(

        UUID channelId,
        String channelName,
        UUID ownerId,
        String channelDescription
) {
}
