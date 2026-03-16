package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends CrudRepository<Message, UUID> {
    void delete(Message message);
    Optional<Message> findLatestMessage(UUID channelId);
    void deleteAllByChannelId(UUID channelId);
    List<Message> findByChannelId(UUID channelId);
}
