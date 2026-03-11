package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;

import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JCFChannelRepository implements ChannelRepository {

    private final Map<String, Channel> data;

    public JCFChannelRepository() {

        data = new HashMap<>();

    }

    @Override
    public boolean saveChannel(Channel channel) {
        if(data.containsKey(channel.getChannelId())){
            return false;
        }
        data.put(channel.getChannelId(), channel);
        return true;

    }

    @Override
    public Channel getChannel(String channelId) {

        return data.getOrDefault(channelId, null);
    }

    @Override
    public List<Channel> getAllChannel() {
        return data.values().stream().toList();
    }

    @Override
    public boolean updateChannel(Channel channel) {



        data.put(channel.getChannelId(), channel);
        return true;

    }

    @Override
    public boolean deleteChannel(String channelId) {

        data.remove(channelId);
        return true;

    }

    @Override
    public boolean isExistChannel(String channelId) {
        return data.containsKey(channelId);
    }
}
