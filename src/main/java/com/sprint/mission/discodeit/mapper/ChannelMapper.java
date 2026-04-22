package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import java.time.Instant;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChannelMapper {

  @Mapping(source = "channel.id", target = "id")
  @Mapping(source = "channel.name", target = "name")
  @Mapping(source = "channel.type", target = "type")
  @Mapping(source = "channel.description", target = "description")
  @Mapping(source = "lastMessageAt", target = "lastMessageAt")
  @Mapping(source = "participants", target = "participants")
  ChannelDto toDto(Channel channel, Instant lastMessageAt, List<UserDto> participants);
}
