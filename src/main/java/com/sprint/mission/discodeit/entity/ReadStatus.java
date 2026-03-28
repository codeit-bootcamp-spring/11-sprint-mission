package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReadStatus extends BaseEntity {
    private UUID userId;
    private UUID channelId;

    public ReadStatus(UUID userId, UUID channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }

    public ReadStatusResponse toResponse() {
        return new ReadStatusResponse(
                this.getId(),
                userId,
                channelId,
                this.getUpdatedAt()
        );
    }
}
