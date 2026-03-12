package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {
    public void init();
    public void save(Message message);
    public Message findById(UUID id);
    public List<Message> findAll();
    public void delete(Message message);
}
