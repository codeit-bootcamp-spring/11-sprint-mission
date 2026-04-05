package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageResponseDto create(UUID channelId, MessageCreateRequestDto dto);

    List<MessageResponseDto> findAllByChannelId(UUID channelId);

    MessageResponseDto update(UUID channelId, UUID messageId, MessageUpdateRequestDto dto);

    void delete(UUID id, UUID messageId);
}
