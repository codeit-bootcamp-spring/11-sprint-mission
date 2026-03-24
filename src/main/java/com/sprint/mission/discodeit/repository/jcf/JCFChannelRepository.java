package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;

import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFChannelRepository implements ChannelRepository {

    private final Map<UUID, Channel> data;

    public JCFChannelRepository(){
        data = new HashMap<>();
    }

    @Override
    public boolean saveChannel(Channel channel) {
        data.put(channel.getId(), channel);
        return true;
    }

    @Override
    public Optional<Channel> getChannel(UUID channelId) {
        return data.containsKey(channelId) ? Optional.of(data.get(channelId)) : Optional.empty();
    }

    @Override
    public List<Channel> getAllChannel() {
        return data.values().stream()
                .toList();
    }

    @Override
    public boolean deleteChannel(UUID channelId) {
        return data.remove(channelId) != null;
    }

    @Override
    public boolean isExistChannel(UUID channelId) {
        return data.containsKey(channelId);
    }
}
