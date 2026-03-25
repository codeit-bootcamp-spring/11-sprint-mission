package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageDto create(MessageCreateRequest request);
    MessageDto findById(UUID id);
    List<MessageDto> findAllByChannelId(UUID channelId);
    void update(UUID id, MessageUpdateRequest request);
    void delete(UUID id);
}
