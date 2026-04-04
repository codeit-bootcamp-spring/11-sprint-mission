package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus implements Serializable {

    // 객체 직렬화
    private static final long serialVersionUID = 1L;

    //필드
    private final UUID id;
    private final Instant createdAt;
    // 연관관계 필드
    private final UUID userId; // User의 UUID id
    private Instant updatedAt;
    private Instant lastOnlineAt;


    public UserStatus(UUID userId, Instant lastOnlineAt) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.lastOnlineAt = lastOnlineAt;
        this.userId = userId;
    }

    // 온라인인지 아닌지 (5분 이내이면 true, 아니면 false)
    public User.Status status() {
        // 현재(Instant.now()) -(minusSeconds) 5분(5 * 60) 이 이후이면(isAfter)
        if (lastOnlineAt.isAfter(Instant.now().minusSeconds(5 * 60))) {
            return User.Status.ONLINE;
        }
        return User.Status.OFFLINE;
    }

    private void update() {
        this.updatedAt = Instant.now();
    }

    public void updateLastOnline() {
        this.lastOnlineAt = Instant.now();
        update();
    }
}
