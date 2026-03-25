package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import java.time.Instant;

@Getter
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    private String username;
    private String email;
    private String password;

    private UUID profileId;

    public User(String username, String email, String password) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.createdAt = now;
        this.updatedAt = now;
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = null;
    }

    public void update(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.updatedAt = Instant.now();
    }

    public void updateProfile(UUID profileId) {
        this.profileId = profileId;
        this.updatedAt = Instant.now();
    }
}