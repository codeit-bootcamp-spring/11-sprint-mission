package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.UUID;

public interface ChannelRepository extends CrudRepository<Channel, UUID> {
    void delete(UUID id);
}
