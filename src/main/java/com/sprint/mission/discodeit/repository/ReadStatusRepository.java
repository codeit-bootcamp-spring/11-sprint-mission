package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReadStatusRepository {
    void save(ReadStatus readStatus);
    ReadStatus findById(UUID id);
    boolean existByUserIdAndChannelId(UUID userId, UUID channelId);
    List<ReadStatus> findAll();
    List<ReadStatus> findAllByChannelId(UUID id);
    List<ReadStatus> findAllByUserId(UUID userId);
    void delete(ReadStatus readStatus);
}
