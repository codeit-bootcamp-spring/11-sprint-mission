package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageDto create(MessageCreateRequest dto);
    MessageDto findById(UUID id);
    List<MessageDto> findAllByChannelId(UUID id);
    void update(UUID id, MessageUpdateRequest dto);
    void delete(UUID id);
}
