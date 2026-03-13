package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record ChannelUpdateRequestDto(
        UUID channelId,
        String name,
        String description
) {
}
