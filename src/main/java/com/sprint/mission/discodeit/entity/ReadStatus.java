package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.ReadStatusResponse;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReadStatus extends BaseEntity {
    private UUID userId;
    private UUID channelId;

    public ReadStatus(User user, Channel channel) {
        this.userId = user.getId();
        this.channelId = channel.getId();
    }

    public ReadStatusResponse toResponse() {
        return new ReadStatusResponse(
                userId,
                channelId,
                getUpdatedAt()
        );
    }
}
