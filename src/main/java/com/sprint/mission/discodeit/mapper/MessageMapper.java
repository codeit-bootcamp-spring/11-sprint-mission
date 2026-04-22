package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public interface MessageMapper {

  @Mapping(target = "id", source = "message.id")
  @Mapping(target = "createdAt", source = "message.createdAt")
  @Mapping(target = "updatedAt", source = "message.updatedAt")
  @Mapping(target = "content", source = "message.content")
  @Mapping(target = "channelId", source = "message.channel.id")
  @Mapping(target = "author", source = "authorDto")
  @Mapping(target = "attachments", source = "message.attachments")
  MessageDto toDto(Message message, UserStatus userStatus, UserDto authorDto);
}