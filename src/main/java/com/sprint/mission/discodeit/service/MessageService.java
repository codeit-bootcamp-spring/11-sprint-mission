package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message create(MessageCreateRequest request);

    Message read(UUID id);

    List<Message> readAllByChannelId(UUID channelId);

    void update(MessageUpdateRequest request);

    void delete(UUID id);
}