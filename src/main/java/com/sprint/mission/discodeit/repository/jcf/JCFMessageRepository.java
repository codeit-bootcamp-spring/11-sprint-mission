package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFMessageRepository implements MessageRepository {

    private final List<Message> data;

    public JCFMessageRepository() {
        this.data = new ArrayList<>();
    }

    @Override
    public Message save(Message message) {
        data.removeIf(m -> m.getId().equals(message.getId()));
        data.add(message);
        return message;
    }

    @Override
    public List<Message> findAll() {
        return data;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return data.stream()
                .filter(m -> m.getId().equals(id))
                .findAny();
    }

    public Optional<Message> findLatestMessageByChannelId(UUID channelId) {
        return data.stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .max(Comparator.comparing(Message::getCreatedAt));
    }

    @Override
    public void delete(UUID id) {
        data.removeIf(m -> m.getId().equals(id));
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        data.removeIf(m -> m.getChannelId().equals(channelId));
    }
}
