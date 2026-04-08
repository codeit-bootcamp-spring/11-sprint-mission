package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    private final List<Message> data;

    public JCFMessageService() {
        this.data = new ArrayList<>();
    }

    @Override
    public void create(Message message) {
        data.add(message);
    }

    @Override
    public Message findById(UUID id) {
        return data.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(data);
    }

    @Override
    public void update(UUID id, String content) {
        Message message = findById(id);
        if (message != null) {
            message.update(content);
        }
    }

    @Override
    public void delete(UUID id) {
        data.removeIf(m -> m.getId().equals(id));
    }
}