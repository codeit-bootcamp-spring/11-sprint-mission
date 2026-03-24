package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFMessageRepository implements MessageRepository {

    private final Map<UUID, Message> data;

    public JCFMessageRepository(){
        data = new HashMap<>();
    }

    @Override
    public boolean saveMessage(Message message) {
        data.put(message.getId(), message);
        return true;
    }

    @Override
    public Optional<Message> getMessage(UUID messageId) {
        return Optional.of(data.get(messageId));

    }

    @Override
    public Optional<Message> getLastMessagebyChannelId(UUID channelId) {
        return data.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .max(Comparator.comparing(Message::getCreatedAt));
    }

    @Override
    public List<Message> getAllMessage() {
        return data.values().stream().toList();
    }

    @Override
    public List<Message> getAllByChannelId(UUID channelId) {
        return data.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .toList();
    }


    @Override
    public boolean deleteMessage(UUID messageId) {
        return data.remove(messageId) != null;
    }

    @Override
    public boolean isExistMessage(UUID messageId) {
        return data.containsKey(messageId);
    }

    @Override
    public boolean channelsMessagedelete(UUID channelId) {
        data.values().removeIf(message -> message.getChannelId()
                .equals(channelId));
        return true;
    }
}
