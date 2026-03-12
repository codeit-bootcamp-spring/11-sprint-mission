package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message create(String contents, UUID userId, UUID channelId);
    Message findById(UUID id);
    List<Message> findAll();
    void update(UUID id, Message newMessage);
    void delete(Message message);
}
