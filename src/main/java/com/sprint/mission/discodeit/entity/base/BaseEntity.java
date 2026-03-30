package com.sprint.mission.discodeit.entity.base;

import lombok.Getter;

import java.time.Instant;

@Getter
public abstract class BaseEntity extends ImmutableBaseEntity {

    protected Instant updatedAt;

    protected BaseEntity() {
        super();
        this.updatedAt = this.createAt;
    }

    protected BaseEntity(BaseEntity other) {
        super(other);
        this.updatedAt = other.updatedAt;
    }

    protected void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    public abstract BaseEntity copy();
}
