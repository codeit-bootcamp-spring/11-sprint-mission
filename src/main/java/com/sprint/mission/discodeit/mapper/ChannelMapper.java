package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class ChannelMapper {

  @Autowired
  protected UserMapper userMapper;

  @Mapping(target = "participants", source = ".", qualifiedByName = "mapParticipants")
  @Mapping(target = "lastMessageAt", source = ".", qualifiedByName = "mapLastMessageAt")
  public abstract ChannelDto toDto(Channel channel);

  @Named("mapParticipants")
  protected List<UserDto> mapParticipants(Channel channel) {
    if (channel == null) {
      return null;
    }

    if (channel.getType() == ChannelType.PRIVATE && channel.getReadStatuses() != null) {
      return channel.getReadStatuses().stream()
          .map(readStatus -> userMapper.toDto(readStatus.getUser()))
          .collect(Collectors.toList());
    } else if (channel.getType() == ChannelType.PUBLIC) {
      return Collections.emptyList();
    }
    return null;
  }

  @Named("mapLastMessageAt")
  protected Instant mapLastMessageAt(Channel channel) {
    if (channel == null || channel.getMessages() == null) {
      return null;
    }

    return channel.getMessages().stream().map(Message::getCreatedAt)
        .max(Instant::compareTo).orElse(null);
  }
}
