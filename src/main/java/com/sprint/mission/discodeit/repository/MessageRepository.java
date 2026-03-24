package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {

    boolean saveMessage(Message message);
    Optional<Message> getMessage(UUID messageId);
    Optional<Message> getLastMessagebyChannelId(UUID channelId);

    List<Message> getAllMessage();

    List<Message> getAllByChannelId(UUID channelId);

    boolean deleteMessage(UUID messageId);
    boolean isExistMessage(UUID messageId);
    boolean channelsMessagedelete(UUID channelId);


}
