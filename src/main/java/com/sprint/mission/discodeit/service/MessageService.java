package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    // Create
//    Message create(String content, UUID channelId, UUID userId);
    Message create(MessageCreateRequest dto);

    // Read
//    Message readAll(UUID id);
    List<Message> findAllByChannelId(UUID channelId);

    // Update
//    Message updateContent(UUID id, String newContent);
    Message update(UUID id, MessageUpdateRequest dto);

    // Delete
    void delete(UUID id);
}
