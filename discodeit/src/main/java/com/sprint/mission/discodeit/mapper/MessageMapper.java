package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.dto.message.MessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class, BinaryContentMapper.class})
public interface MessageMapper {

    @Mapping(target = "channelId", source = "channel.id")
    MessageDto toDto(Message message);
}
