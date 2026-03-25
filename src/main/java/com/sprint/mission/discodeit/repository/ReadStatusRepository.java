package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository {
    void save(ReadStatus readStatus);
    List<ReadStatus> findByChannelId(UUID channelId);
    List<ReadStatus> findByUserId(UUID userId);
    void deleteByChannelId(UUID channelId);
    ReadStatus findById(UUID id);
    void delete(UUID id);
    boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);
}
