package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import java.time.Instant;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChannelMapper {

  @Mapping(target = "type", expression = "java(channel.getType().name())")
  @Mapping(target = "participants", source = "participants")
  @Mapping(target = "lastMessageAt", source = "lastMessageAt")
  ChannelDto toDto(Channel channel, List<UserDto> participants, Instant lastMessageAt);
}