package com.sprint.mission.discodeit.entity.baseentity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Common implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private Instant createdAt;

    public Common() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

}
