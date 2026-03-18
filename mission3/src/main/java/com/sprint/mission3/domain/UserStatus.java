package com.sprint.mission3.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor

public class UserStatus {
    private final Instant lastActivityAt;

    public boolean isOnline() {
        // 현재기준 5분 이내일 경유 true
        return lastActivityAt.isAfter(Instant.now().minusSeconds((5*60));
    }
}
