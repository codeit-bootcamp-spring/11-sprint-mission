package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID currentUserId;
    private final long createdAt;
    private long updatedAt;
    private String username;
    private String password;
    private String email;

    // 생성자 초기화
    public User(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.currentUserId = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // 필드를 수정하는 update 함수
    public void update(String name, String email, String password) {
        this.username = name;
        this.email = email;
        this.password = password;
        this.updatedAt = System.currentTimeMillis();
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
