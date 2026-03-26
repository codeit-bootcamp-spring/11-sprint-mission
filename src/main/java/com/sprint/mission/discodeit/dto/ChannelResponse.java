package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        String channelName,
        String description,
        ChannelType type,
        List<UUID> participantsIds,
        Instant lastMessageAt
) {

    public static ChannelResponse of(Channel channel, Instant lastMessageAt) {
        return new ChannelResponse(
                channel.getId(),
                channel.getChannelName(),
                channel.getDescription(),
                channel.getType(),
                channel.getParticipantIds(),
                lastMessageAt
        );
    }
}
