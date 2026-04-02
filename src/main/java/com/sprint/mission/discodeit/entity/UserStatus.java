package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserStatus extends BaseEntity {
    private final UUID userId;

    public UserStatus(UUID userId) {
        this.userId = userId;
    }
}
