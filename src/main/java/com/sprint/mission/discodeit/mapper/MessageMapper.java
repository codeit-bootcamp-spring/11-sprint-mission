package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public abstract class MessageMapper {

  @Autowired
  protected UserMapper userMapper;

  @Mapping(source = "message.channel.id", target = "channelId")
  @Mapping(target = "author", expression = "java(message.getAuthor() != null ? userMapper.toDto(message.getAuthor(), message.getAuthor().getStatus()) : null)")
  public abstract MessageDto toDto(Message message);
}
