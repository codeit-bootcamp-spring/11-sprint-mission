package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    private final Map<UUID, Message> data;

    private final ChannelService channelService;
    private final UserService userService;

    public JCFMessageService() {
        this.data = new HashMap<>();
        this.channelService = null;
        this.userService = null;
    }

    public JCFMessageService(ChannelService channelService, UserService userService) {
        this.data = new HashMap<>();
        this.channelService = channelService;
        this.userService = userService;
    }

    @Override
    public Message create(String content, UUID channelId, UUID authorId) {
        channelService.find(channelId);
        userService.find(authorId);

        Message message = new Message(content, channelId, authorId);
        this.data.put(message.getId(), message);
        return message;
    }

    @Override
    public Message find(UUID messageId) {
        Message messageNullable = this.data.get(messageId);
        return Optional.ofNullable(messageNullable)
                .orElseThrow(() -> new NoSuchElementException("Message with id " + messageId + " not found"));
    }

    @Override
    public List<Message> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public Message update(UUID messageId, String newContent) {
        Message message = find(messageId);
        message.update(newContent);
        return message;
    }

    @Override
    public void delete(UUID messageId) {
        if (!data.containsKey(messageId)) {
            throw new NoSuchElementException("Message with id " + messageId + " not found");
        }
        data.remove(messageId);
    }
}
