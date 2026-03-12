package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final long createdAt;
    private long updatedAt;
    private String username;
    private String password;
    private String email;

    // 생성자 초기화
    public User(String username, String email, String password) {
        this.id = UUID.randomUUID();
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

    // id 반환
    public UUID getId() {
        return id;
    }

    // 생성시간 반환
    public String getCreatedAt() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault());
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return formattedDate;
    }

    // 업데이트 시간 반환
    public String getUpdatedAt() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault());
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return formattedDate;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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
