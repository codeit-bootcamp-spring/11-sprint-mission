package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
// application.yaml의 설정값에 따라 Bean을 설정 / name : 설정값의 이름, havingValue : type 지정, matchIfMissing : 설정이 안되있으면 jcf
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFChannelRepository implements ChannelRepository {

    private final Map<UUID, Channel> channels = new HashMap<>();

    @Override
    public void insert(Channel channel) {
        channels.put(channel.getId(), channel);
    }

    @Override
    public Channel findById(UUID id) {
        Channel channel = channels.get(id);
        if (channel == null) {
            throw new NoSuchElementException("해당 채널은 존재하지 않습니다. id : " + id);
        }
        return channel;
    }

    @Override
    public List<Channel> findAll() {
        return this.channels.values().stream().toList();
    }

    @Override
    public void update(Channel channel) {
        channels.put(channel.getId(), channel);
    }

    @Override
    public void delete(UUID id) {
        channels.remove(id);
    }
}
