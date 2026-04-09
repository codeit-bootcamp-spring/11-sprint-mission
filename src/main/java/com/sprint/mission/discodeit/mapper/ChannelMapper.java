package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChannelMapper {

  public ChannelDto toDto(Channel channel, Instant lastMessageAt,
      List<UserDto> participants) {
    if (channel == null) {
      return null;
    }

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
