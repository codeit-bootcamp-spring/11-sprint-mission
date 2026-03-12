package com.sprint.mission.discodeit.entity.Domain;

import lombok.Getter;

import java.util.UUID;

@Getter
public class BaseEntity {
    protected UUID id;      // 유효아이디
    protected Long createdAt;   // 만든시간
    protected Long updatedAt;   // 만든걸 업데이트한 시간

    public BaseEntity() {
        this.id = UUID.randomUUID();            // 유효아이디(각 개체마다)
        this.createdAt = System.currentTimeMillis();    // 각각 만들어진 시간
        this.updatedAt = this.createdAt;
    }

    protected void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
    // 업데이트된 시간 = 호출될때
}
