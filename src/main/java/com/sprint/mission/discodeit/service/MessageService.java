package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    //create, read, readAll, update, delete

    void create(MessageCreateRequest request);
    Message read(UUID id);
    List<Message> readAllByChannelId(UUID channelId);
    void update(UUID messageId, MessageUpdateRequest request);
    void delete(UUID id);

    void setUserService(UserService userService);
    void setChannelService(ChannelService channelService);

    void clearMessagesInChannel(UUID channelId);
    void clearMessagesByUser(UUID userId);
}
