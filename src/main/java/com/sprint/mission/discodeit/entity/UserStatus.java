package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserStatus extends BaseEntity {
    private UUID userId;

    public UserStatus(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserStatus{" +
                "userId=" + userId +
                '}';
    }
}
