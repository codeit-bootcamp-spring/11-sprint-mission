package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import java.util.Collections;

public class MessageMapper {

  public static MessageDto toDto(Message message) {
    if (message == null) {
      return null;
    }

    return new MessageDto(
        message.getId(),
        message.getCreatedAt(),
        message.getUpdatedAt(),
        message.getContent(),
        message.getChannel().getId(),
        // User 엔티티를 UserDto로 변환 (UserMapper 구현 필요)
        UserMapper.toDto(message.getAuthor()),
        // 현재 단계에서는 첨부파일을 빈 리스트로 처리
        Collections.emptyList()
    );
  }
}