package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.sprint.mission.discodeit.entity.Message;

@Mapper(componentModel = "spring", uses = {UserMapper.class, BinaryContentMapper.class})
public interface MessageMapper {

  // "channel.id" = getChannel().getId()
  @Mapping(target = "channelId", source = "channel.id")
  MessageDto toDto(Message message);

  // author(User -> UserDto)와 attachments(List -> List)는 MapStruct가 uses에 등록된 매퍼들을 써서 알아서 변환해준다.
}
