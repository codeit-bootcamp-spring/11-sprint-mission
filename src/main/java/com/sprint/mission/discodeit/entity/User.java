package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private UUID profileId;
    private final Instant createdAt;
    private Instant updatedAt;
    private String username;
    private String password;
    private String email;


    public User(String username, String email, String password, UUID profileId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
        this.updatedAt = Instant.now();
    }

    // 필드를 수정하는 update 함수
    public void update(String name, String email, String password, UUID profileId) {
        this.username = name;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
        this.updatedAt = Instant.now();
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
