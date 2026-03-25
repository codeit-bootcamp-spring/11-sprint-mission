package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateDto;
import com.sprint.mission.discodeit.dto.MessageUpdateDto;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    // Create
//    Message create(String content, UUID channelId, UUID userId);
    Message create(MessageCreateDto dto);

    // Read
//    Message readAll(UUID id);
    List<Message> findAllByChannelId(UUID channelId);

    // Update
//    Message updateContent(UUID id, String newContent);
    Message update(UUID id, MessageUpdateDto dto);

    // Delete
    void delete(UUID id);
}
