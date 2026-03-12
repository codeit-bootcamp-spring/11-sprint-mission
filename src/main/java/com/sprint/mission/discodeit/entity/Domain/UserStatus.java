package com.sprint.mission.discodeit.entity.Domain;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    // UserId 기반
    // 마지막 접속시간 lastOnlineAt
    // user가 online인지? > boolean isOnline
    private UUID userId;
    private Instant lastOnlineAt;

    public UserStatus(UUID userId, Instant lastOnlineAt) {
        this.userId = userId;
        this.lastOnlineAt = lastOnlineAt;
    }

    public void updateLastOnlineAt(Instant lastOnlineAt) {  // 마지막 접속시간 설정하는 메소드
        this.lastOnlineAt = lastOnlineAt;
    }

    public boolean isOnline() {      // 마지막 접속시간보다 300초 이전 true, 이후 false
        return Instant.now().minusSeconds(300).isBefore(this.lastOnlineAt); // true = online, false = offline
    }
}