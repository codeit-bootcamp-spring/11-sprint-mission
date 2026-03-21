package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
