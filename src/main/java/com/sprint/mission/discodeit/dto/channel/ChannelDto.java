package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.Channel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelDto(
        UUID id,
        String type,
        String name,
        String description,
        List<UUID> participantIds,
        Instant lastMessageAt
) {
    public static ChannelDto from(Channel channel, List<UUID> participantIds) {
        return new ChannelDto(
                channel.getId(),
                channel.getType().name(),
                channel.getName(),
                channel.getDescription(),
                participantIds,
                channel.getRecentMessageTime()
        );
    }
}