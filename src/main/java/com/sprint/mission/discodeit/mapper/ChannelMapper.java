package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channeldto.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelMapper {


  private final UserMapper userMapper;

  public ChannelDto toDto(Channel channel, List<User> participants, Instant lastMessageTime) {

    return new ChannelDto(

        channel.getId(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        participants.stream().map(userMapper::toDto).toList(),
        lastMessageTime
    );
  }

}
