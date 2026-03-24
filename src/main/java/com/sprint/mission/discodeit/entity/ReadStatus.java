package com.sprint.mission.discodeit.entity;


import lombok.Getter;

import java.util.UUID;

@Getter
public class ReadStatus extends Entity{

    private final UUID channelId;
    private final UUID userId;

    public ReadStatus(UUID userId,UUID channelId) {
       this.userId = userId;
        this.channelId = channelId;
    }
}
