package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
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
    public void createChannel(Channel channel) {
        channelList.add(channel);
    }

    @Override
    public Channel findChannel(UUID id) {
        for(Channel channel: channelList) {
            if(channel.getId().equals(id)) return channel;
        }
        throw new IllegalArgumentException("Channel Not Found");
    }

    @Override
    public List<Channel> findAllChannel() {
        return channelList;
    }

    @Override
    public void updateChannel(Channel oldChannel, Channel newChannel) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        oldChannel.setName(newChannel.getName());
        oldChannel.update();
    }

    @Override
    public void deleteChannel(Channel channel) {
        channelList.remove(channel);
    }
}
