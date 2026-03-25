package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {
    ReadStatus save(ReadStatus status);
    List<ReadStatus> findAllByChannelId(UUID channelId);
    List<ReadStatus> findAllByUserId(UUID userId);
    Optional<ReadStatus> find(UUID id);
    boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);
    void delete(UUID id);
    void deleteByChannelId(UUID channelId);
}
