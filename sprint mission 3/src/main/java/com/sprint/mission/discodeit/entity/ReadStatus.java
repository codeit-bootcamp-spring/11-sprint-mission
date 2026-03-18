package com.sprint.mission.discodeit.entity;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus extends BaseEntity implements Serializable {
    private UUID userId;
    private UUID channelId;
    private Instant lastMessageAt;
    private static final long serialVersionUID = 1L;

    public ReadStatus(UUID userId, UUID channelId, Instant lastMessageAt) {
        super();
        this.userId = userId;
        this.channelId = channelId;
        this.lastMessageAt = lastMessageAt;
    }

    // 마지막으로 메세지 읽은 시간
    public void updateLastMessageReadAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
        updateTimestamp();
    }
}
