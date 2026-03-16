package com.sprint.mission3.domain;

import lombok.Getter; // Lombok 임포트

import java.time.Instant;

@Getter

public class User {
    private Long id;
    private String name;
    private String email;

    private User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}

private Instant createdAt; // 세계 표준 시간
