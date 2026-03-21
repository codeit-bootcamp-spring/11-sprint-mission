package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Common implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;

    public Common() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update() {
        this.updatedAt = Instant.now();
    }

}
