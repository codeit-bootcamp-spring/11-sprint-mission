package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class User implements Serializable {
    private UUID id;
    private UUID profileId;
    private Instant createdAt;
    private Instant updatedAt;
    private String name;
    private String email;
    private String password;
    private static final long serialVersionUID = 1L;

    public User(String name, String email, String password) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Instant update(String name) {
        this.name = name;
        return updatedAt = Instant.now();
    }

    public void updateProfile(UUID profileId) {
        this.profileId = profileId;
        this.updatedAt = Instant.now();
    }
}