package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFMessageRepository implements MessageRepository {
    List<Message> messages;

    public JCFMessageRepository() {
        init();
    }

    @Override
    public void init() {
        messages = new ArrayList<>();
    }

    @Override
    public void save(Message message) {
        messages.add(message);
    }

    @Override
    public Message load(UUID id) {
        List<Message> list;
        list = messages.stream()
                .filter(p -> p.getId().equals(id))
                .toList();
        if(list.isEmpty()) {
            throw new IllegalArgumentException("Message Not Found: " + id);
        }
        return list.get(0);
    }

    @Override
    public List<Message> loadAll() {
        return messages;
    }

    @Override
    public void delete(Message message) {
        messages.remove(message);
    }
}
