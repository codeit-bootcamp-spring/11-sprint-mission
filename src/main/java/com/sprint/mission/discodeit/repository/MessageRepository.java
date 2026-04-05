package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    Message save(Message message);
    List<Message> findAll();
    List<Message> findAllByChannelId(UUID chanelId);
    Optional<Message> findById(UUID id);
    Optional<Message> findLatestMessageByChannelId(UUID id);
    void delete(UUID id);
    void deleteByChannelId(UUID channelId);
}