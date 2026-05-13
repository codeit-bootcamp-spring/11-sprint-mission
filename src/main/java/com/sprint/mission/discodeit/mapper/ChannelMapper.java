package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ChannelMapper {

    @Mapping(target = "id", source = "channel.id")
    @Mapping(target = "name", source = "channel.name")
    @Mapping(target = "description", source = "channel.description")
    @Mapping(target = "type", source = "channel.type")
    @Mapping(target = "participants", source = "participants")
    @Mapping(target = "lastMessageAt", source = "lastMessageAt")
    ChannelDto toDto(Channel channel, List<UserDto> participants, Instant lastMessageAt);
}