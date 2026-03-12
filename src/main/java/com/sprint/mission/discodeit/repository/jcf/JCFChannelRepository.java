package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelRepository implements ChannelRepository {
    List<Channel> channels;

    public JCFChannelRepository() {
        init();
    }

    @Override
    public void init() {
        channels = new ArrayList<>();
    }

    @Override
    public void save(Channel channel) {
        channels.add(channel);
    }

    @Override
    public Channel load(UUID id) {
        List<Channel> list;
        list = channels.stream()
                .filter(p -> p.getId().equals(id))
                .toList();
        if(list.isEmpty()) {
            throw new IllegalArgumentException("Channel Not Found: " + id);
        }
        return list.get(0);
    }

    @Override
    public List<Channel> loadAll() {
        return channels;
    }

    @Override
    public void delete(Channel channel) {
        channels.remove(channel);
    }
}
