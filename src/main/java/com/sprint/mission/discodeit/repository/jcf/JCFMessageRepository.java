package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// @Repository
public class JCFMessageRepository implements MessageRepository {

    private final Map<UUID, Message> messageData = new ConcurrentHashMap<>();

    @Override
    public void save(Message message) {
        messageData.put(message.getId(), message);
    }

    @Override
    public Message findById(UUID id) {
        Message message = messageData.get(id);
        if (message != null && message.isDeleted()) {
            return null;
        }
        return message;
    }

    @Override
    public List<Message> findAll() {
        if (messageData.isEmpty()) {
            return new ArrayList<>();
        }
        return messageData.values().stream()
                .filter(message -> !message.isDeleted())
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        Message message = findById(id);
        if (message != null) {
            message.softDelete();
            save(message);
        }
    }
}