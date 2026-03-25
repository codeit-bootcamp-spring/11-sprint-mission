/*
package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    private final Map<UUID, Message> data;

    private final UserService userService;
    private final ChannelService channelService;

    public JCFMessageService(UserService userService, ChannelService channelService) {
        this.data = new HashMap<>();
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public Message create(String content, UUID authorId, UUID channelId, List<UUID> attachmentIds) {
        userService.findById(authorId); // 유저 검증
        Channel channel = channelService.findById(channelId); // 채널 검증

        // 채널 접근 권한 검증
        if (channel.getType() == ChannelType.PRIVATE || channel.getType() == ChannelType.DM) {
            if (channel.getMemberIds() == null || !channel.getMemberIds().contains(authorId)) {
                throw new IllegalArgumentException("User " + authorId + " is not a member of channel " + channelId);
            }
        }

        Message message = new Message(content, authorId, channelId, attachmentIds);
        data.put(message.getId(), message);
        return message;
    }

    @Override
    public Message findById(UUID id) {
        return Optional.ofNullable(data.get(id))
                .orElseThrow(() -> new NoSuchElementException("Message with id " + id + " not found"));
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Message update(UUID id, String content) {
        Message message = findById(id);
        message.update(content);
        return message;
    }

    @Override
    public void delete(UUID id) {
        findById(id);
        data.remove(id);
    }
}*/
