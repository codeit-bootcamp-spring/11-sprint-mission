package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus implements Serializable {

    // 객체 직렬화
    private static final long serialVersionUID = 1L;

    // 필드
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastReadAt;

    // 연관관계 필드
    private final UUID userId; // User의 UUID id
    private final UUID channelId; // Channel의 UUID id
//    private final UUID messageId; // 특정 메시지를 읽은 시간이 아닌 채널 별 마지막으로 읽은 시간이기 때문에 X

    // 생성자
    public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.lastReadAt = lastReadAt;
        this.userId = userId;
        this.channelId = channelId;
    }

    // update
    private void update() {
        this.updatedAt = Instant.now();
    }
    public void updateLastReadAt() {
        this.lastReadAt = Instant.now();
        update();
    }
}
