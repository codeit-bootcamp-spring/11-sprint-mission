package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.baseentity.UpdatableEntity;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReadStatus extends UpdatableEntity {
    private UUID userId;
    private UUID channelId;

    public ReadStatus(UUID userId, UUID channelId) {
        super();
        this.userId = userId;
        this.channelId = channelId;
    }
}
