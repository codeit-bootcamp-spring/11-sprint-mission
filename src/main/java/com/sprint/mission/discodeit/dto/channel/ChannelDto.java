package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelDto(
    UUID id,
    String name,
    ChannelType type,
    String description,
    Instant lastMessageAt,
    List<UserDto> participants
) {

  public static ChannelDto from(Channel channel, Instant lastMessageAt,
      List<UserDto> participants) {
    return new ChannelDto(
        channel.getId(),
        channel.getName(),
        channel.getType(),
        channel.getDescription(),
        lastMessageAt,
        participants
    );
  }
}
