package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message createMessage(UUID channelId, UUID authorId, String message);
    Message readMessage(UUID id);
    public List<Message> readMessagesByChannel(UUID channelId);
    List<Message> readAllMessage();
    void deleteMessage(UUID id);
    void updateMessage(UUID id, String message);
}
