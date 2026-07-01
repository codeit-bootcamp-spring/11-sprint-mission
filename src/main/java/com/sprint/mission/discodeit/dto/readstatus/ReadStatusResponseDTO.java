package com.sprint.mission.discodeit.dto.readstatus;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusResponseDTO(
        UUID readStatusId,
        UUID userId,
        UUID channelId,
        Instant lastReadAt
) {
    public static ReadStatusResponseDTO from(ReadStatus readStatus) {
        return new ReadStatusResponseDTO(
                readStatus.getId(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getLastReadAt()
        );
    }
}