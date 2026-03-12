package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.ReadStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatus createReadStatus(UUID userId, UUID channelId);
    ReadStatus getReadStatusById(UUID id);
    ReadStatus getReadStatusByUserIdAndChannelId(UUID userId, UUID channelId);
    List<ReadStatus> getReadStatusesByUserId(UUID userId);
    List<ReadStatus> getReadStatusesByChannelId(UUID channelId);
    List<ReadStatus> getAllReadStatuses();
    void updateReadStatus(UUID id, Instant lastReadAt);
    void deleteReadStatus(UUID id);
}
