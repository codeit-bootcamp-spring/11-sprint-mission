package com.sprint.mission.discodeit.entity.baseentity;

import lombok.Getter;

import java.time.Instant;

@Getter
public class UpdatableEntity extends Common {
    private Instant updatedAt;

    public UpdatableEntity() {
        super();
        this.updatedAt = Instant.now();
    }

    public void update() {
        this.updatedAt = Instant.now();
    }
}
