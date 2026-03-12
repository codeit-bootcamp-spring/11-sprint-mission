package com.sprint.mission.discodeit.entity.Domain;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

// User가 Channel별 마지막으로 읽은 메세지 시간 표현, User별 각 채널에 읽지 않은 메세지 확인용
/*  User, Channel, lastMessageReadAt >> 이렇게 3개 필요
    User, Channel은 UUID로 식별 / lastMessageReadAt은 Instant로 하기 */
@Getter
public class ReadStatus implements Serializable {   // extends 필요없어보임. 어차피 ㅁ
    private UUID userId;
    private UUID channelId;
    private Instant lastMessageReadAt;
    private Instant createdAt;
    private static final long serialVersionUID = 1L;

    public ReadStatus(UUID userId, UUID channelId, Instant lastMessageReadAt) {
        this.userId = userId;
        this.channelId = channelId;
        this.lastMessageReadAt = lastMessageReadAt;
        this.createdAt = Instant.now();
    }

    public void updateLastMessageReadAt(Instant lastMessageReadAt) {
        this.lastMessageReadAt = lastMessageReadAt;
    }
}
