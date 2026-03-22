package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
// @Repository
public class JCFChannelRepository implements ChannelRepository {

    private final Map<UUID, Channel> channelData = new ConcurrentHashMap<>();

    @Override
    public void save(Channel channel) {
        channelData.put(channel.getId(), channel);
    }

    @Override
    public Channel findById(UUID id) {
        Channel channel = channelData.get(id);
        if (channel != null && channel.isDeleted()) {
            return null;
        }
        return channel;
    }

    @Override
    public List<Channel> findAll() {
        if (channelData.isEmpty()) {
            return new ArrayList<>();
        }
        return channelData.values().stream()
                .filter(channel -> !channel.isDeleted())
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        Channel channel = findById(id);
        if (channel != null) {
            channel.softDelete();
            save(channel);
        }
    }
}