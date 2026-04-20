package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {

  private final BinaryContentMapper binaryContentMapper;
  private final UserMapper userMapper;

  public MessageDto toDto(Message message) {
    return new MessageDto(
        message.getId(), // id
        message.getCreatedAt(), // createdAt
        message.getUpdatedAt(), // updatedAt
        message.getContent(), // content
        message.getChannel().getId(), // channelId
        userMapper.toDto(message.getAuthor()), // author(UserDto)
        message.getAttachments().stream()
            .map(binaryContentMapper::toDto)
            .toList() // attachments(BinaryContentDto)
    );
  }

}
