package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record ChannelUpdateParam(
        UUID id,
        ChannelUpdateRequest request
) {
}
