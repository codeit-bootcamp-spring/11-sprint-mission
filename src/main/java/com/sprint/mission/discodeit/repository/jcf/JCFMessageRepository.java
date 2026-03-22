package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> data;

    public JCFMessageRepository() {
        this.data = new HashMap<>();
    }

    @Override
    public void save(Message message) {
        this.data.put(message.getId(), message);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(this.data.values());
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return this.data.values().stream()
                .filter(message -> message.getChannel().getId().equals(channelId))
                .toList();
    }

    @Override
    public void delete(Message message) {
        this.data.remove(message.getId());
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        this.data.values().removeIf(message -> message.getChannel().getId().equals(channelId));
    }
}
