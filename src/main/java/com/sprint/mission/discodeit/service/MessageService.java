package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequestDto;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message create(MessageCreateRequestDto dto);
    Message findById(UUID id);
    List<Message> findAllByChannelId(UUID id);
    void update(MessageUpdateRequestDto dto);
    void delete(UUID id);
}
