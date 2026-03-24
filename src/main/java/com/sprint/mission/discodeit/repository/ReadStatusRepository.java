package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {
    
    boolean save(ReadStatus readStatus);
    Optional<ReadStatus> get(UUID userId, UUID channelId);
    List<ReadStatus> getAll();
    List<ReadStatus> getAllByUserId(UUID userId);
    List<ReadStatus> getAllByChannelId(UUID channelId);
    boolean delete(UUID userId, UUID channelId);
    boolean isExist(UUID userId, UUID channelId);
    
    
}
