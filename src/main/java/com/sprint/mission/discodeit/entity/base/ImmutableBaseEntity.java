package com.sprint.mission.discodeit.entity.base;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class ImmutableBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    protected UUID id;
    protected Instant createAt;

    protected ImmutableBaseEntity() {
        this.id = UUID.randomUUID();
        this.createAt = Instant.now(); // ms 단위
    }

    protected ImmutableBaseEntity(ImmutableBaseEntity other) {
        this.id = other.id;
        this.createAt = other.createAt;
    }

    public abstract ImmutableBaseEntity copy();
}
