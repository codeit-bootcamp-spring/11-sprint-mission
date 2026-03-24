package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelService implements ChannelService {
    private final List<Channel> data;

    public JCFChannelService() {
        this.data = new ArrayList<>();
    }

    @Override
    public void create(Channel channel) {
        data.add(channel);
    }

    @Override
    public Channel findById(UUID id) {
        return data.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(data);
    }

    @Override
    public void update(UUID id, String name, String description) {
        Channel channel = findById(id);
        if (channel != null) {
            channel.update(name, description);
        }
    }

    @Override
    public void delete(UUID id) {
        data.removeIf(c -> c.getId().equals(id));
    }
}