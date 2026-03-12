package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelService implements ChannelService {
    final List<Channel> channelList;

    public JCFChannelService() {
        channelList = new ArrayList<>();
    }

    @Override
    public Channel create(ChannelType channelType, String name, String description) {
        Channel channel = new Channel(channelType, name, description);
        channelList.add(channel);
        return channel;
    }

    @Override
    public Channel findById(UUID id) {
        for(Channel channel: channelList) {
            if(channel.getId().equals(id)) return channel;
        }
        throw new IllegalArgumentException("Channel Not Found");
    }

    @Override
    public List<Channel> findAll() {
        return channelList;
    }

    @Override
    public void update(Channel oldChannel, Channel newChannel) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        oldChannel.setChannelType(newChannel.getChannelType());
        oldChannel.setName(newChannel.getName());
        oldChannel.setDescription(newChannel.getDescription());
        oldChannel.update();
    }

    @Override
    public void delete(Channel channel) {
        channelList.remove(channel);
    }
}
