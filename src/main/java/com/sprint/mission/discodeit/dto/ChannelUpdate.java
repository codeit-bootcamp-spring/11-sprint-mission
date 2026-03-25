package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record ChannelUpdate(
        UUID id,
        String name,
        String description
) {
}
