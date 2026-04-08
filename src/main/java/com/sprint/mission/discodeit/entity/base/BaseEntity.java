package com.sprint.mission.discodeit.entity.base;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BaseEntity{
    private final UUID id;
    private final Instant createdAt;

    public BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

}
