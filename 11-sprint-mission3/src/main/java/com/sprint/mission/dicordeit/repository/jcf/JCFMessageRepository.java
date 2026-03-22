package com.sprint.mission.dicordeit.repository.jcf;

import com.sprint.mission.dicordeit.entity.Channel;
import com.sprint.mission.dicordeit.entity.Message;
import com.sprint.mission.dicordeit.repository.MessageRepository;

import java.util.*;

public class JCFMessageRepository implements MessageRepository {

    private final Map<UUID, Message> data = new HashMap<>();

    @Override
    public void save(Message message) {
        data.put(message.getId(), message);
    }

    @Override
    public Message findById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }


}
