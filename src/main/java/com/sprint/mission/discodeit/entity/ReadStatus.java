package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class ReadStatus extends BaseEntity {

    private UUID userId;
    private UUID channelId;
    private Instant lastReadAt;

    private ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
        super();
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadAt = lastReadAt;
    }

    protected ReadStatus(ReadStatus other) {
        super(other);
        this.userId = other.userId;
        this.channelId = other.channelId;
        this.lastReadAt = other.lastReadAt;
    }

    @Override
    public ReadStatus copy() {
        return new ReadStatus(this);
    }

    public static ReadStatus create(UUID userId, UUID channelId) {
        return new ReadStatus(userId, channelId, Instant.now());
    }

    // 이하 로직
    public void updateReadAt() {
        this.lastReadAt = Instant.now();
        touch();
    }
}

