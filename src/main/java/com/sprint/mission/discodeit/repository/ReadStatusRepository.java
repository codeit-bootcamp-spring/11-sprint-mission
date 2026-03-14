package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository {
    List<ReadStatus> findAllByChannelId(UUID id);
    void deleteAllByChannelId(UUID id);
}
