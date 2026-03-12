package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel createChannel(String name, String description, ChannelType type, UUID ownerId);
    Channel readChannel(UUID id);
    List<Channel> allReadChannel();
    void deleteChannel(UUID id);
    void updateChannel(UUID id, String name, String description);
}
