package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.*;

public class JCFMessageService {
    private final Map<UUID, Message> data;

    private final UserService userService;
    private final ChannelService channelService;

    public JCFMessageService(UserService userService, ChannelService channelService) {
        this.data = new HashMap<>();
        this.userService = userService;
        this.channelService = channelService;
    }


    public Message create(UUID userId, UUID channelId, String content) {
        if (userService.find(userId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자");
        }
        if (channelService.find(channelId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 채널");
        }
        Message message = new Message(userId, channelId, content);
        data.put(message.getId(), message);
        return message;
    }

    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    public List<Message> findAll() {
        return new ArrayList<>(data.values());
    }

    public Message update(UUID id, String content) {
        Message message = data.get(id);

        if (message == null) {
            return null;
        }

        message.update(content);
        return message;
    }

    public void delete(UUID id) {
        data.remove(id);
    }
}
