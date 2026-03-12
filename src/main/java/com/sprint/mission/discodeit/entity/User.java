package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID currentUserId;
    private final UUID profileId;
    private final Instant createdAt;
    private Instant updatedAt;
    private String username;
    private String password;
    private String email;

    // 필드를 수정하는 update 함수
    public void update(String name, String email, String password) {
        this.username = name;
        this.email = email;
        this.password = password;
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
