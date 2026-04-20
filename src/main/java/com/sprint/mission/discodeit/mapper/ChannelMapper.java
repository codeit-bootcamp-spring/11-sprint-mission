package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

  private final MessageMapper messageMapper;
  private final ReadStatusMapper readStatusMapper;
  private final UserMapper userMapper;

  public ChannelDto toDto(Channel channel, List<User> participants, Instant lastMessageAt) {
    return new ChannelDto(
        channel.getId(), // id
        channel.getType(), // type(Public, Private)
        channel.getName(), // name
        channel.getDescription(), // description
        participants.stream()
            .map(userMapper::toDto)
            .toList(),
        lastMessageAt
    );
  }

}
