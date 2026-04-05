package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class User implements Serializable {     //Serializable 구현
    private static final long serialVersionUID = 1L;

    // filed
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;
    private String username;
    private String email;
    private String password;        // password field 추가
    private byte[] profileImageContent;
    private String profileImageContentType;
    private Boolean online;

    // 생성자
    public User(String username, String email, String password, Object o, Object object) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String username, String email, String password, byte[] profileImageContent, String profileImageContentType, Boolean online) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
        this.username = username;
        this.email = email;
        this.password = password;       // password 할당
        this.profileImageContent = profileImageContent;
        this.profileImageContentType = profileImageContentType;
        this.online = online;
    }

    // 기존 이미지 없는 생성자
    public User(String username, String email, String password) {
        this(username, email, password, null, null);
    }

    //비즈니스 메서드
    // update 메서드 (이미지도 변경 가능)
    public void update(String username, String email, String password, byte[] profileImageContent, String profileImageContentType) {
        if (username != null && !username.isEmpty()) {
            this.username = username;
        }
        if (email != null && !email.isEmpty()) {
            this.email = email;
        }
        if (password != null && !password.isEmpty()) {
            this.password = password;
        }
        if (profileImageContent != null) {
            this.profileImageContent = profileImageContent;
        }
        if (profileImageContentType != null && !profileImageContentType.isEmpty()) {
            this.profileImageContentType = profileImageContentType;
        }
        this.updatedAt = Instant.now();
    }
}