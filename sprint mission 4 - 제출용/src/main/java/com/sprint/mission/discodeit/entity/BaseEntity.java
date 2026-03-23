package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    protected UUID id;
    protected Instant createdAt;
    protected Instant updatedAt;

    // 엔티티 생성 시 기본값 초기화
    public BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // 엔티티 수정 시 updateAt갱신
    protected void updateTimestamp() {
        this.updatedAt = Instant.now();
    }
}
