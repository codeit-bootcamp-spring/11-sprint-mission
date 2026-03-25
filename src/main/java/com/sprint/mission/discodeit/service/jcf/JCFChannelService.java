package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.*;

public class JCFChannelService {
    private final Map<UUID, Channel> data;

    public JCFChannelService() {
        this.data = new HashMap<>();
    }


    public Channel create(String name, String description) {
        Channel channel = new Channel(name, description);
        data.put(channel.getId(), channel);
        return channel;
    }


    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }


    public List<Channel> findAll() {
        return new ArrayList<>(data.values());
    }


    public Channel update(UUID id, String name, String description) {
        Channel channel = data.get(id);

        if (channel == null) {
            return null;
        }
        channel.update(name, description);
        return channel;
    }


    public void delete(UUID id) {
        data.remove(id);
    }
}
