package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class BaseEntity implements Serializable {

    private UUID id;
    // Instant 로 변경
    private Instant createdAt;
    private Instant updatedAt;

    private boolean isDeleted;

    public BaseEntity(){
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void updateTime(){
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.isDeleted = true;
        updateTime();
    }

    public void restore() {
        this.isDeleted = false;
        updateTime();
    }
}