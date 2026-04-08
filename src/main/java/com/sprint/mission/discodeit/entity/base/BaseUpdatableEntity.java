package com.sprint.mission.discodeit.entity.base;

import lombok.Getter;

import java.time.Instant;

@Getter
public class BaseUpdatableEntity extends BaseEntity {
    private Instant updatedAt;

    public BaseUpdatableEntity() {
        super();
        this.updatedAt = Instant.now();
    }

    public void update() {
        this.updatedAt = Instant.now();
    }
}
