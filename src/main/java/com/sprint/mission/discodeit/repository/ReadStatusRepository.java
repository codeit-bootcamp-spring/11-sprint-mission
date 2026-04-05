package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {
    ReadStatus save(ReadStatus readStatus);

    Optional<ReadStatus> findById(UUID id);

    List<ReadStatus> findAllByUserId(UUID userId);

    List<ReadStatus> findAllByChannelId(UUID channelId);

    List<ReadStatus> findAllByChannelIdAndMessageId(UUID channelId, UUID messageId);

    void deleteAllByChannelId(UUID channelId);

    void deleteById(UUID id);

}
