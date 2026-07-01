package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.base.Repository;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository extends Repository<ReadStatus, UUID> {
    List<ReadStatus> findByUserId(UUID userId);

    List<ReadStatus> findByChannelId(UUID channelId);
}
