package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChannelMapper {

  private final UserMapper userMapper;

  public ChannelResponse toResponse(Channel channel, List<User> participants,
      Instant lastMessageAt) {
    return new ChannelResponse(
        channel.getId(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        participants.stream()
            .map(this.userMapper::toResponse)
            .toList(),
        lastMessageAt
    );
  }
}