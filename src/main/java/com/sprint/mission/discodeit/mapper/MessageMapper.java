package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Context;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class, UserMapper.class})
public interface MessageMapper {

    @Mapping(target = "channelId", source = "channel.id")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "attachments", source = "attachments")
    MessageDto toDto(Message message, @Context Set<UUID> onlineUserIds);
}
