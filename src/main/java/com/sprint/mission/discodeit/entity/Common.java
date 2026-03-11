package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class Common implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private Long createdAt;
    private Long updatedAt;

    public Common() {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public void update() {
        this.updatedAt = System.currentTimeMillis();
    }

}
