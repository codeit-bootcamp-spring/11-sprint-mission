package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.UUID;

public record ChannelDto(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        ChannelType type
) {
    public static ChannelDto from(Channel channel) {
        return new ChannelDto(
                channel.getId(),
                channel.getOwnerId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType()
        );
    }
}
