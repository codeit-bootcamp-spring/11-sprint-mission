package com.sprint.mission.discodeit.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public abstract class Entity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID id; // 인스턴스 아이디
    private final Long createdAt; //생성 시간
    private Long updatedAt; //변경 시간




    public Entity() {

        id = UUID.randomUUID();
        createdAt = System.currentTimeMillis();
        updatedAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void updateUpdatedAt(){

        updatedAt   = System.currentTimeMillis();

    }


    public int compareTo(Entity entity) {
        return this.createdAt.compareTo(entity.getCreatedAt());
    }
}
