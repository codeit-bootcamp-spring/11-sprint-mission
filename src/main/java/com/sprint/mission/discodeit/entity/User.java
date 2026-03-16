package com.sprint.mission.discodeit.entity;

import java.io.Serializable;  // 추가
import java.util.UUID;

public class User implements Serializable {  // 추가

    private final UUID id;
    private final long createdAt;
    private long updatedAt;
    private String name;

    // 기본 생성자 (Serializable 필수)
    public User() {
        this.id = null;           // final 필드 초기화
        this.createdAt = 0L;      // final 필드 초기화
    }

    public User(String name) {
        this.id = UUID.randomUUID();
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
        this.name = name;
    }

    // getter들 (전부 있어야 함)
    public UUID getId() { return id; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public String getName() { return name; }

    public void update(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }
}
