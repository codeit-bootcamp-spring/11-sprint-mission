package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.UUID;

@Getter // 모든 필드의 Getter를 자동으로 생성합니다.
public class User implements Serializable {
    private final UUID id;
    private final Long createdAt;

    @Setter // name과 email은 수정이 필요하므로 Setter를 추가해줍니다.
    private String name;
    @Setter
    private String email;
    private Long updatedAt;

    public User(String name, String email) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.name = name;
        this.email = email;
    }

}