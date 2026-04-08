package com.sprint.mission.discodeit.entity.baseentity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BaseEntity implements Serializable{
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final Instant createdAt;

    public BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

}
