package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository {
    ReadStatus create(ReadStatus readStatus);

    ReadStatus read(UUID id);

    ReadStatus readByUserIdAndChannelId(UUID userId, UUID channelId);

    List<ReadStatus> readAllByChannelId(UUID channelId);

    List<ReadStatus> readAllByUserId(UUID userId);

    ReadStatus update(UUID userId, UUID channelId, Instant lastMessageReadAt);

    void deleteByChannelId(UUID channelId);

    void deleteByUserId(UUID userId);
}

