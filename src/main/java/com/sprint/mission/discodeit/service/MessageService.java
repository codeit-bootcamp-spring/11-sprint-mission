package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.message.MessageResponseDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageResponseDto create(MessageCreateRequestDto dto);
    MessageResponseDto findById(UUID id);
    List<MessageResponseDto> findAllByChannelId(UUID id);
    void update(UUID id, MessageUpdateRequestDto dto);
    void delete(UUID id);
}
