package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;

public interface MessageRepository {

    boolean saveMessage(Message message);
    Message getMessage(String messageId);
    List<Message> getAllMessage();
    boolean updateMessage(Message message);
    boolean deleteMessage(String messageId);
    boolean isExistMessage(String messageId);
    boolean channelsMessagedelete(String channelId);


}
