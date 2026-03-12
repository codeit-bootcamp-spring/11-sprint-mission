package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelRepository {
    public void init();
    public void save(Channel channel);
    public Channel findById(UUID id);
    public List<Channel> findAll();
    public void delete(Channel channel);
}
