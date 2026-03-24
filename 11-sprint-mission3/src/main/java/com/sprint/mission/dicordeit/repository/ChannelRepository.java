package com.sprint.mission.dicordeit.repository;

import com.sprint.mission.dicordeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelRepository {

    void save(Channel channel);

    Channel findById(UUID id);

    List<Channel> findAll();

    void delete(UUID id);

}
