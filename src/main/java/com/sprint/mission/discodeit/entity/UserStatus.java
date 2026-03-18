package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID userId;
    private Instant lastOnlineAt;

    public UserStatus(UUID userId, Instant lastOnlineAt) {
        this.userId = userId;
        this.lastOnlineAt = lastOnlineAt;
    }

    public void updateLastOnlineAt(Instant lastOnlineAt) {  // 마지막 접속시간 설정하는 메소드
        this.lastOnlineAt = lastOnlineAt;
    }

    // 마지막 접속시간이 5분이내일시 접속 중
    public boolean isOnline() {
        return Instant.now().minusSeconds(300).isBefore(this.lastOnlineAt);
    }
}