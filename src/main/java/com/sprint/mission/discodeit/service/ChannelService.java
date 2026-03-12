package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel create(ChannelType channelType, String name, String description);
    Channel findById(UUID id);
    List<Channel> findAll();
    void update(Channel oldChannel, Channel newChannel);
    void delete(Channel channel);
}
