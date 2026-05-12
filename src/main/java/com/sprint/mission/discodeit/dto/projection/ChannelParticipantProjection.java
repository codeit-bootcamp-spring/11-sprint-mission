package com.sprint.mission.discodeit.dto.projection;

import com.sprint.mission.discodeit.entity.User;

import java.util.UUID;

public record ChannelParticipantProjection(
        UUID channelId,
        User participant
) {
}
